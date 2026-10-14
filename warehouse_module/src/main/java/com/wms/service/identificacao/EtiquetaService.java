package com.wms.service.identificacao;

import com.wms.domain.auditoria.TransactionLog;
import com.wms.domain.base.BaseEntity;
import com.wms.domain.enums.MovementType;
import com.wms.domain.identificacao.MovimentacaoEstoque;
import com.wms.domain.identificacao.Selo;
import com.wms.repository.identificacao.MovimentacaoEstoqueRepository;
import com.wms.repository.identificacao.SeloRepository;
import com.wms.service.cache.SeloCacheService;
import com.wms.service.transaction.RedisTransactionStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class EtiquetaService {
    private final SeloRepository seloRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final RedisTransactionStore transactionStore;
    private final SeloCacheService seloCacheService;

    public EtiquetaService(
            SeloRepository seloRepository,
            MovimentacaoEstoqueRepository movimentacaoRepository,
            RedisTransactionStore transactionStore,
            SeloCacheService seloCacheService) {
        this.seloRepository = seloRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.transactionStore = transactionStore;
        this.seloCacheService = seloCacheService;
    }

    @Transactional
    public Selo registrarEntrada(
            String code,
            Long productId,
            Long batchId,
            Long locationId,
            BigDecimal quantity,
            BigDecimal weight,
            Long userId,
            String userName) {
        validateCode(code);
        validatePositiveQuantity(quantity);
        validateActor(userId, userName);
        if (productId == null || locationId == null) {
            throw new IllegalArgumentException("Produto e local de destino são obrigatórios");
        }
        if (weight != null && weight.signum() < 0) {
            throw new IllegalArgumentException("O peso não pode ser negativo");
        }
        if (seloRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Já existe uma etiqueta com o código " + code);
        }

        LocalDateTime now = LocalDateTime.now();
        Selo selo = new Selo();
        selo.setCode(code);
        selo.setProductId(productId);
        selo.setBatchId(batchId);
        selo.setLocationId(locationId);
        selo.setQuantity(quantity);
        selo.setQuantityAvailable(quantity);
        selo.setQuantityReserved(BigDecimal.ZERO);
        selo.setQuantityCanceled(BigDecimal.ZERO);
        selo.setWeight(weight);
        selo.setActive(true);
        setCreationAuditFields(selo, userName, now);
        Selo savedSelo = seloRepository.save(selo);
        seloCacheService.cache(savedSelo);

        MovimentacaoEstoque movement = new MovimentacaoEstoque();
        movement.setProductId(productId);
        movement.setBatchId(batchId);
        movement.setDestinationLocationId(locationId);
        movement.setMovementType(MovementType.ENTRADA);
        movement.setQuantity(quantity);
        movement.setWeight(weight);
        movement.setMovementDate(now);
        movement.setUserName(userName);
        setCreationAuditFields(movement, userName, now);
        MovimentacaoEstoque savedMovement = movimentacaoRepository.save(movement);

        saveTransaction(savedSelo, savedMovement, userId, "Entrada de etiqueta: " + code);
        return savedSelo;
    }

    @Transactional
    public Selo registrarSaida(String code, BigDecimal quantity, Long userId, String userName) {
        validateCode(code);
        validatePositiveQuantity(quantity);
        validateActor(userId, userName);

        Selo selo = seloRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Etiqueta não encontrada: " + code));
        if (!Boolean.TRUE.equals(selo.getActive())) {
            throw new IllegalStateException("A etiqueta está inativa: " + code);
        }
        if (selo.getQuantityAvailable() == null || selo.getQuantityAvailable().compareTo(quantity) < 0) {
            throw new IllegalArgumentException("Quantidade solicitada maior que o saldo disponível da etiqueta");
        }

        LocalDateTime now = LocalDateTime.now();
        selo.setQuantityAvailable(selo.getQuantityAvailable().subtract(quantity));
        if (selo.getQuantityAvailable().signum() == 0) {
            selo.setActive(false);
        }
        setModificationAuditFields(selo, userName, now);
        Selo savedSelo = seloRepository.save(selo);
        seloCacheService.cache(savedSelo);

        MovimentacaoEstoque movement = new MovimentacaoEstoque();
        movement.setProductId(selo.getProductId());
        movement.setBatchId(selo.getBatchId());
        movement.setOriginLocationId(selo.getLocationId());
        movement.setMovementType(MovementType.SAIDA);
        movement.setQuantity(quantity);
        movement.setMovementDate(now);
        movement.setUserName(userName);
        setCreationAuditFields(movement, userName, now);
        MovimentacaoEstoque savedMovement = movimentacaoRepository.save(movement);

        saveTransaction(savedSelo, savedMovement, userId, "Saída de etiqueta: " + code);
        return savedSelo;
    }

    @Transactional(readOnly = true)
    public Optional<Selo> consultarPorCodigo(String code) {
        validateCode(code);
        return seloRepository.findByCode(code);
    }

    private void saveTransaction(Selo selo, MovimentacaoEstoque movement, Long userId, String reason) {
        TransactionLog transaction = new TransactionLog();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setMovementId(movement.getId());
        transaction.setUserId(userId);
        transaction.setOriginLocationId(movement.getOriginLocationId());
        transaction.setDestinationLocationId(movement.getDestinationLocationId());
        transaction.setTransactionDate(movement.getMovementDate());
        transaction.setSealId(selo.getId());
        transaction.setProductId(selo.getProductId());
        transaction.setBatchId(selo.getBatchId());
        transaction.setReason(reason);
        transaction.setMovementType(movement.getMovementType());
        transaction.setQuantity(movement.getQuantity());
        transaction.setWeight(movement.getWeight());
        transactionStore.save(transaction);
    }

    private void setCreationAuditFields(
            BaseEntity entity,
            String userName,
            LocalDateTime timestamp) {
        entity.setUserNameCreator(userName);
        entity.setCreationDate(timestamp);
        setModificationAuditFields(entity, userName, timestamp);
    }

    private void setModificationAuditFields(
            BaseEntity entity,
            String userName,
            LocalDateTime timestamp) {
        entity.setUserNameLastChange(userName);
        entity.setLastChangeDate(timestamp);
    }

    private void validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("O código da etiqueta é obrigatório");
        }
    }

    private void validatePositiveQuantity(BigDecimal quantity) {
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero");
        }
    }

    private void validateActor(Long userId, String userName) {
        if (userId == null || userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("O identificador e o nome do operador são obrigatórios");
        }
    }
}
