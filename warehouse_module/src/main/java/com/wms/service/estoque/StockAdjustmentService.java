package com.wms.service.estoque;

import com.wms.domain.auditoria.TransactionLog;
import com.wms.domain.base.BaseEntity;
import com.wms.domain.enums.MovementType;
import com.wms.domain.enums.StatusOperacao;
import com.wms.domain.estoque.Estoque;
import com.wms.domain.identificacao.MovimentacaoEstoque;
import com.wms.domain.operacoes.Inventario;
import com.wms.domain.operacoes.ajustes.HistoricoAjuste;
import com.wms.domain.operacoes.ajustes.TipoAjuste;
import com.wms.domain.operacoes.itens.InventarioItem;
import com.wms.integration.estoque.StockAdjustmentRequest;
import com.wms.repository.estoque.EstoqueRepository;
import com.wms.repository.identificacao.MovimentacaoEstoqueRepository;
import com.wms.repository.operacoes.HistoricoAjusteRepository;
import com.wms.repository.operacoes.InventarioItemRepository;
import com.wms.repository.operacoes.InventarioRepository;
import com.wms.service.cache.EstoqueCacheService;
import com.wms.service.transaction.RedisTransactionStore;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class StockAdjustmentService {
    private final InventarioItemRepository inventoryItemRepository;
    private final InventarioRepository inventoryRepository;
    private final EstoqueRepository stockRepository;
    private final MovimentacaoEstoqueRepository movementRepository;
    private final HistoricoAjusteRepository historyRepository;
    private final EstoqueCacheService stockCacheService;
    private final RedisTransactionStore transactionStore;

    public StockAdjustmentService(
            InventarioItemRepository inventoryItemRepository,
            InventarioRepository inventoryRepository,
            EstoqueRepository stockRepository,
            MovimentacaoEstoqueRepository movementRepository,
            HistoricoAjusteRepository historyRepository,
            EstoqueCacheService stockCacheService,
            RedisTransactionStore transactionStore) {
        this.inventoryItemRepository = inventoryItemRepository;
        this.inventoryRepository = inventoryRepository;
        this.stockRepository = stockRepository;
        this.movementRepository = movementRepository;
        this.historyRepository = historyRepository;
        this.stockCacheService = stockCacheService;
        this.transactionStore = transactionStore;
    }

    @Transactional
    public AdjustmentResult adjust(Long inventoryItemId, StockAdjustmentRequest request) {
        if (inventoryItemId == null || inventoryItemId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O item de inventário deve ser válido");
        }
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A solicitação de ajuste é obrigatória");
        }
        try {
            request.validate();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }

        InventarioItem item = inventoryItemRepository.findActiveByIdForUpdate(inventoryItemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Item de inventário não encontrado ou inativo"));
        validateItemMatchesRequest(item, request);

        Inventario inventory = inventoryRepository.findByIdAndActiveTrue(item.getInventoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Inventário não encontrado ou inativo"));
        if (inventory.getStatus() == StatusOperacao.CONCLUIDO
                || inventory.getStatus() == StatusOperacao.CANCELADO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível ajustar um inventário concluído ou cancelado");
        }

        List<Estoque> stockEntries = stockRepository.lockActiveByProductAndLocation(
                item.getProductId(), item.getBatchId(), item.getLocationId(), null);
        BigDecimal systemQuantity = totalQuantity(stockEntries);
        BigDecimal countedQuantity = request.countedQuantity();
        BigDecimal quantityDifference = countedQuantity.subtract(systemQuantity);
        BigDecimal systemWeight = request.countedWeight() == null ? null : totalWeight(stockEntries);
        BigDecimal weightDifference = request.countedWeight() == null
                ? null
                : request.countedWeight().subtract(systemWeight);
        LocalDateTime now = LocalDateTime.now();

        if (quantityDifference.signum() == 0
                && (weightDifference == null || weightDifference.signum() == 0)) {
            item.setQuantityCounted(countedQuantity);
            if (request.countedWeight() != null) {
                item.setWeightCounted(request.countedWeight());
            }
            setModificationAuditFields(item, request.userName(), now);
            return new AdjustmentResult(false, null, BigDecimal.ZERO, BigDecimal.ZERO, null);
        }

        applyCountedBalances(item, stockEntries, request, quantityDifference, weightDifference, now);
        item.setQuantityCounted(countedQuantity);
        if (request.countedWeight() != null) {
            item.setWeightCounted(request.countedWeight());
        }
        setModificationAuditFields(item, request.userName(), now);

        MovimentacaoEstoque movement = createMovement(item, request, quantityDifference, weightDifference, now);
        MovimentacaoEstoque savedMovement = movementRepository.save(movement);
        HistoricoAjuste history = createHistory(
                item, request, savedMovement, systemQuantity, systemWeight,
                countedQuantity, quantityDifference, weightDifference, now);
        history = historyRepository.save(history);
        saveTransaction(item, request, savedMovement, quantityDifference, weightDifference, now);

        return new AdjustmentResult(true, history.getAdjustmentType().name(),
                quantityDifference.abs(),
                weightDifference == null ? BigDecimal.ZERO : weightDifference.abs(),
                history.getId());
    }

    private void validateItemMatchesRequest(InventarioItem item, StockAdjustmentRequest request) {
        if (!Objects.equals(item.getProductId(), request.productId())
                || !Objects.equals(item.getLocationId(), request.locationId())
                || !Objects.equals(item.getBatchId(), request.batchId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Produto, lote e local devem corresponder ao item do inventário");
        }
    }

    private BigDecimal totalQuantity(List<Estoque> stockEntries) {
        return stockEntries.stream()
                .map(stock -> {
                    if (stock.getQuantity() == null) {
                        throw new IllegalStateException("A quantidade do estoque não foi inicializada");
                    }
                    return stock.getQuantity();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal totalWeight(List<Estoque> stockEntries) {
        return stockEntries.stream()
                .map(stock -> stock.getWeight() == null ? BigDecimal.ZERO : stock.getWeight())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void applyCountedBalances(
            InventarioItem item,
            List<Estoque> stockEntries,
            StockAdjustmentRequest request,
            BigDecimal quantityDifference,
            BigDecimal weightDifference,
            LocalDateTime now) {
        if (stockEntries.isEmpty()) {
            Estoque stock = new Estoque();
            stock.setProductId(item.getProductId());
            stock.setBatchId(item.getBatchId());
            stock.setLocationId(item.getLocationId());
            stock.setQuantity(request.countedQuantity());
            stock.setQuantityReserved(BigDecimal.ZERO);
            stock.setQuantityAvailable(request.countedQuantity());
            stock.setWeight(request.countedWeight() == null ? BigDecimal.ZERO : request.countedWeight());
            setCreationAuditFields(stock, request.userName(), now);
            saveStock(stock);
            return;
        }

        Set<Estoque> changedStocks = Collections.newSetFromMap(new IdentityHashMap<>());
        validateReservations(stockEntries, request.countedQuantity());
        if (quantityDifference.signum() > 0) {
            Estoque stock = stockEntries.get(0);
            validateBalances(stock);
            stock.setQuantity(stock.getQuantity().add(quantityDifference));
            stock.setQuantityAvailable(stock.getQuantityAvailable().add(quantityDifference));
            setModificationAuditFields(stock, request.userName(), now);
            changedStocks.add(stock);
        } else if (quantityDifference.signum() < 0) {
            reduceAvailableStock(stockEntries, quantityDifference.abs(), request.userName(), now, changedStocks);
        }

        if (weightDifference != null && weightDifference.signum() != 0) {
            adjustWeight(stockEntries, weightDifference, request.userName(), now, changedStocks);
        }
        changedStocks.forEach(this::saveStock);
    }

    private void validateReservations(List<Estoque> stockEntries, BigDecimal countedQuantity) {
        BigDecimal reserved = stockEntries.stream()
                .map(stock -> {
                    if (stock.getQuantityReserved() == null) {
                        throw new IllegalStateException("A quantidade reservada do estoque não foi inicializada");
                    }
                    return stock.getQuantityReserved();
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (countedQuantity.compareTo(reserved) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A contagem não pode ficar abaixo da quantidade reservada");
        }
    }

    private void reduceAvailableStock(
            List<Estoque> stockEntries,
            BigDecimal quantityToReduce,
            String userName,
            LocalDateTime now,
            Set<Estoque> changedStocks) {
        BigDecimal remaining = quantityToReduce;
        for (Estoque stock : stockEntries) {
            if (remaining.signum() == 0) {
                break;
            }
            validateBalances(stock);
            BigDecimal reserved = stock.getQuantityReserved();
            if (reserved == null) {
                throw new IllegalStateException("A quantidade reservada do estoque não foi inicializada");
            }
            BigDecimal reducible = stock.getQuantityAvailable()
                    .min(stock.getQuantity().subtract(reserved).max(BigDecimal.ZERO));
            BigDecimal reduction = reducible.min(remaining);
            if (reduction.signum() == 0) {
                continue;
            }
            stock.setQuantity(stock.getQuantity().subtract(reduction));
            stock.setQuantityAvailable(stock.getQuantityAvailable().subtract(reduction));
            setModificationAuditFields(stock, userName, now);
            changedStocks.add(stock);
            remaining = remaining.subtract(reduction);
        }
        if (remaining.signum() > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível reduzir o saldo abaixo das quantidades reservadas");
        }
    }

    private void adjustWeight(
            List<Estoque> stockEntries,
            BigDecimal difference,
            String userName,
            LocalDateTime now,
            Set<Estoque> changedStocks) {
        if (difference.signum() > 0) {
            Estoque stock = stockEntries.get(0);
            stock.setWeight((stock.getWeight() == null ? BigDecimal.ZERO : stock.getWeight()).add(difference));
            setModificationAuditFields(stock, userName, now);
            changedStocks.add(stock);
            return;
        }

        BigDecimal remaining = difference.abs();
        for (Estoque stock : stockEntries) {
            if (remaining.signum() == 0) {
                break;
            }
            BigDecimal currentWeight = stock.getWeight() == null ? BigDecimal.ZERO : stock.getWeight();
            BigDecimal reduction = currentWeight.min(remaining);
            if (reduction.signum() == 0) {
                continue;
            }
            stock.setWeight(currentWeight.subtract(reduction));
            setModificationAuditFields(stock, userName, now);
            changedStocks.add(stock);
            remaining = remaining.subtract(reduction);
        }
        if (remaining.signum() > 0) {
            throw new IllegalStateException("O peso total do estoque é menor que a redução solicitada");
        }
    }

    private void validateBalances(Estoque stock) {
        if (stock.getQuantity() == null || stock.getQuantityAvailable() == null) {
            throw new IllegalStateException("Os saldos do estoque não foram inicializados");
        }
    }

    private void saveStock(Estoque stock) {
        Estoque savedStock = stockRepository.save(stock);
        stockCacheService.cache(savedStock);
    }

    private MovimentacaoEstoque createMovement(
            InventarioItem item,
            StockAdjustmentRequest request,
            BigDecimal quantityDifference,
            BigDecimal weightDifference,
            LocalDateTime now) {
        MovimentacaoEstoque movement = new MovimentacaoEstoque();
        movement.setProductId(item.getProductId());
        movement.setBatchId(item.getBatchId());
        movement.setOriginLocationId(item.getLocationId());
        movement.setMovementType(MovementType.AJUSTE);
        movement.setQuantity(quantityDifference.abs());
        movement.setWeight(weightDifference == null ? null : weightDifference.abs());
        movement.setMovementDate(now);
        movement.setUserName(request.userName());
        setCreationAuditFields(movement, request.userName(), now);
        return movement;
    }

    private HistoricoAjuste createHistory(
            InventarioItem item,
            StockAdjustmentRequest request,
            MovimentacaoEstoque movement,
            BigDecimal systemQuantity,
            BigDecimal systemWeight,
            BigDecimal countedQuantity,
            BigDecimal quantityDifference,
            BigDecimal weightDifference,
            LocalDateTime now) {
        HistoricoAjuste history = new HistoricoAjuste();
        history.setInventoryId(item.getInventoryId());
        history.setInventoryItemId(item.getId());
        history.setMovementId(movement.getId());
        history.setProductId(item.getProductId());
        history.setBatchId(item.getBatchId());
        history.setLocationId(item.getLocationId());
        history.setSystemQuantity(systemQuantity);
        history.setCountedQuantity(countedQuantity);
        history.setDifferenceQuantity(quantityDifference.abs());
        history.setSystemWeight(systemWeight);
        history.setCountedWeight(request.countedWeight());
        history.setDifferenceWeight(weightDifference == null ? null : weightDifference.abs());
        BigDecimal direction = quantityDifference.signum() == 0
                ? Objects.requireNonNull(weightDifference, "A divergência de peso deve ser informada")
                : quantityDifference;
        history.setAdjustmentType(direction.signum() > 0 ? TipoAjuste.SOBRA : TipoAjuste.PERDA);
        history.setReason(request.reason().getDescricao());
        history.setUserId(request.userId());
        history.setAdjustmentDate(now);
        setCreationAuditFields(history, request.userName(), now);
        return history;
    }

    private void saveTransaction(
            InventarioItem item,
            StockAdjustmentRequest request,
            MovimentacaoEstoque movement,
            BigDecimal quantityDifference,
            BigDecimal weightDifference,
            LocalDateTime now) {
        TransactionLog transaction = new TransactionLog();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setMovementId(movement.getId());
        transaction.setUserId(request.userId());
        transaction.setOriginLocationId(item.getLocationId());
        transaction.setDestinationLocationId(null);
        transaction.setTransactionDate(now);
        transaction.setSealId(item.getSealId());
        transaction.setProductId(item.getProductId());
        transaction.setBatchId(item.getBatchId());
        transaction.setReason(request.reason().getDescricao());
        transaction.setMovementType(MovementType.AJUSTE);
        transaction.setQuantity(quantityDifference.abs());
        transaction.setWeight(weightDifference == null ? null : weightDifference.abs());
        transaction.setAdditionalData("{\"inventoryId\":" + item.getInventoryId()
                + ",\"inventoryItemId\":" + item.getId() + "}");
        transactionStore.save(transaction);
    }

    private void setCreationAuditFields(BaseEntity entity, String userName, LocalDateTime timestamp) {
        entity.setUserNameCreator(userName);
        entity.setCreationDate(timestamp);
        setModificationAuditFields(entity, userName, timestamp);
    }

    private void setModificationAuditFields(BaseEntity entity, String userName, LocalDateTime timestamp) {
        entity.setUserNameLastChange(userName);
        entity.setLastChangeDate(timestamp);
    }

    public record AdjustmentResult(
            boolean adjusted,
            String adjustmentType,
            BigDecimal differenceQuantity,
            BigDecimal differenceWeight,
            Long historyId) {
    }
}
