package com.wms.service.estoque;

import com.wms.domain.base.BaseEntity;
import com.wms.domain.enums.MovementType;
import com.wms.domain.enums.StatusOperacao;
import com.wms.domain.estoque.Estoque;
import com.wms.domain.identificacao.MovimentacaoEstoque;
import com.wms.domain.operacoes.OrdemEstoque;
import com.wms.domain.operacoes.complementos.OrdemEstoqueItem;
import com.wms.integration.estoque.OrderMovementRequest;
import com.wms.repository.estoque.EstoqueRepository;
import com.wms.repository.identificacao.MovimentacaoEstoqueRepository;
import com.wms.repository.operacoes.OrdemEstoqueItemRepository;
import com.wms.repository.operacoes.OrdemEstoqueRepository;
import com.wms.service.cache.EstoqueCacheService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderMovementProcessor {
    private final OrdemEstoqueRepository orderRepository;
    private final OrdemEstoqueItemRepository orderItemRepository;
    private final MovimentacaoEstoqueRepository movementRepository;
    private final EstoqueRepository stockRepository;
    private final EstoqueCacheService estoqueCacheService;

    public OrderMovementProcessor(
            OrdemEstoqueRepository orderRepository,
            OrdemEstoqueItemRepository orderItemRepository,
            MovimentacaoEstoqueRepository movementRepository,
            EstoqueRepository stockRepository,
            EstoqueCacheService estoqueCacheService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.movementRepository = movementRepository;
        this.stockRepository = stockRepository;
        this.estoqueCacheService = estoqueCacheService;
    }

    @Transactional
    public OrdemEstoque process(OrderMovementRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("A solicitação da ordem não foi informada");
        }
        request.validate();

        var existingOrder = orderRepository.findByCode(request.code());
        if (existingOrder.isPresent()) {
            return existingOrder.orElseThrow();
        }

        LocalDateTime now = LocalDateTime.now();
        List<Estoque> stockEntries = stockRepository.lockActiveByProductAndLocation(
                request.productId(), request.batchId(), request.locationId(), request.logisticUnitId());
        if (request.movementType() == MovementType.ENTRADA) {
            registerEntry(request, stockEntries, now);
        } else {
            registerExit(request, stockEntries, now);
        }

        OrdemEstoque order = createOrder(request, now);
        order = orderRepository.save(order);

        OrdemEstoqueItem item = new OrdemEstoqueItem();
        item.setOrderId(order.getId());
        item.setProductId(request.productId());
        item.setBatchId(request.batchId());
        item.setLogisticUnitId(request.logisticUnitId());
        item.setQuantity(request.quantity());
        item.setWeight(request.weight());
        item.setExecutedQuantity(request.quantity());
        item.setExecutedWeight(request.weight());
        setCreationAuditFields(item, request.userName(), now);
        orderItemRepository.save(item);

        MovimentacaoEstoque movement = new MovimentacaoEstoque();
        movement.setOrderId(order.getId());
        movement.setProductId(request.productId());
        movement.setBatchId(request.batchId());
        movement.setLogisticUnitId(request.logisticUnitId());
        movement.setMovementType(request.movementType());
        movement.setQuantity(request.quantity());
        movement.setWeight(request.weight());
        movement.setMovementDate(now);
        movement.setUserName(request.userName());
        if (request.movementType() == MovementType.ENTRADA) {
            movement.setDestinationLocationId(request.locationId());
        } else {
            movement.setOriginLocationId(request.locationId());
        }
        setCreationAuditFields(movement, request.userName(), now);
        movementRepository.save(movement);

        return order;
    }

    private OrdemEstoque createOrder(OrderMovementRequest request, LocalDateTime now) {
        OrdemEstoque order = new OrdemEstoque();
        order.setCode(request.code());
        order.setOrderType(request.movementType());
        order.setStatus(StatusOperacao.CONCLUIDO);
        order.setReasonId(request.reasonId());
        if (request.movementType() == MovementType.ENTRADA) {
            order.setDestinationLocationId(request.locationId());
        } else {
            order.setOriginLocationId(request.locationId());
        }
        order.setRequestDate(now);
        order.setExecutionDate(now);
        order.setUserId(request.userId());
        order.setExecutionUserId(request.userId());
        order.setObservation(request.observation());
        setCreationAuditFields(order, request.userName(), now);
        return order;
    }

    private void registerEntry(OrderMovementRequest request, List<Estoque> stockEntries, LocalDateTime now) {
        Estoque stock = stockEntries.isEmpty() ? new Estoque() : stockEntries.get(0);
        if (stockEntries.isEmpty()) {
            stock.setProductId(request.productId());
            stock.setBatchId(request.batchId());
            stock.setLocationId(request.locationId());
            stock.setLogisticUnitId(request.logisticUnitId());
            stock.setQuantity(BigDecimal.ZERO);
            stock.setQuantityReserved(BigDecimal.ZERO);
            stock.setQuantityAvailable(BigDecimal.ZERO);
            stock.setWeight(BigDecimal.ZERO);
            setCreationAuditFields(stock, request.userName(), now);
        } else {
            validateStockBalances(stock);
            setModificationAuditFields(stock, request.userName(), now);
        }
        stock.setQuantity(stock.getQuantity().add(request.quantity()));
        stock.setQuantityAvailable(stock.getQuantityAvailable().add(request.quantity()));
        if (request.weight() != null) {
            stock.setWeight((stock.getWeight() == null ? BigDecimal.ZERO : stock.getWeight()).add(request.weight()));
        }
        stockRepository.save(stock);
        estoqueCacheService.cache(stock);
    }

    private void registerExit(OrderMovementRequest request, List<Estoque> stockEntries, LocalDateTime now) {
        BigDecimal available = stockEntries.stream()
                .map(Estoque::getQuantityAvailable)
                .map(quantity -> {
                    if (quantity == null) {
                        throw new IllegalStateException("O saldo disponível do estoque não foi inicializado");
                    }
                    return quantity;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (available.compareTo(request.quantity()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente para a saída solicitada");
        }

        BigDecimal remaining = request.quantity();
        for (Estoque stock : stockEntries) {
            if (remaining.signum() == 0) {
                break;
            }
            validateStockBalances(stock);
            BigDecimal quantityAvailable = stock.getQuantityAvailable();
            BigDecimal deducted = quantityAvailable.min(remaining);
            if (stock.getQuantity().compareTo(deducted) < 0) {
                throw new IllegalStateException("A quantidade total do estoque é menor que o saldo disponível");
            }
            stock.setQuantity(stock.getQuantity().subtract(deducted));
            stock.setQuantityAvailable(quantityAvailable.subtract(deducted));
            if (request.weight() != null && stock.getWeight() != null && stock.getWeight().signum() > 0) {
                BigDecimal weightDeducted = request.weight()
                        .multiply(deducted)
                        .divide(request.quantity(), 4, RoundingMode.HALF_UP);
                stock.setWeight(stock.getWeight().subtract(weightDeducted).max(BigDecimal.ZERO));
            }
            setModificationAuditFields(stock, request.userName(), now);
            stockRepository.save(stock);
            estoqueCacheService.cache(stock);
            remaining = remaining.subtract(deducted);
        }
    }

    private void validateStockBalances(Estoque stock) {
        if (stock.getQuantity() == null || stock.getQuantityAvailable() == null) {
            throw new IllegalStateException("Os saldos do estoque não foram inicializados");
        }
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
}
