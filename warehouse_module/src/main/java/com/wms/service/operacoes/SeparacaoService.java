package com.wms.service.operacoes;

import com.wms.domain.auditoria.TransactionLog;
import com.wms.domain.base.BaseEntity;
import com.wms.domain.enums.MovementType;
import com.wms.domain.enums.StatusOperacao;
import com.wms.domain.identificacao.MovimentacaoEstoque;
import com.wms.domain.identificacao.Selo;
import com.wms.domain.operacoes.Pedido;
import com.wms.domain.operacoes.Separacao;
import com.wms.domain.operacoes.ajustes.MotivoMovimentacao;
import com.wms.domain.operacoes.itens.PedidoItem;
import com.wms.domain.operacoes.itens.SeparacaoItem;
import com.wms.integration.estoque.OrderMovementRequest;
import com.wms.repository.identificacao.MovimentacaoEstoqueRepository;
import com.wms.repository.identificacao.SeloRepository;
import com.wms.service.auditoria.CrudAuditService;
import com.wms.service.estoque.OrderMovementProcessor;
import com.wms.service.transaction.RedisTransactionStore;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SeparacaoService {
    private final EntityManager entityManager;
    private final CrudAuditService auditService;
    private final SeloRepository seloRepository;
    private final MovimentacaoEstoqueRepository movementRepository;
    private final OrderMovementProcessor orderMovementProcessor;
    private final RedisTransactionStore transactionStore;

    public SeparacaoService(
            EntityManager entityManager,
            CrudAuditService auditService,
            SeloRepository seloRepository,
            MovimentacaoEstoqueRepository movementRepository,
            OrderMovementProcessor orderMovementProcessor,
            RedisTransactionStore transactionStore) {
        this.entityManager = entityManager;
        this.auditService = auditService;
        this.seloRepository = seloRepository;
        this.movementRepository = movementRepository;
        this.orderMovementProcessor = orderMovementProcessor;
        this.transactionStore = transactionStore;
    }

    @Transactional
    public Separacao iniciar(Separacao separacao) {
        if (separacao == null || separacao.getOrderId() == null || separacao.getOrderId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O pedido da separação é obrigatório.");
        }
        if (separacao.getUserName() == null || separacao.getUserName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O operador da separação é obrigatório.");
        }
        Pedido pedido = entityManager.find(Pedido.class, separacao.getOrderId());
        if (pedido == null || !Boolean.TRUE.equals(pedido.getActive())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O pedido não existe ou está inativo.");
        }

        LocalDateTime now = LocalDateTime.now();
        separacao.setId(null);
        separacao.setStatus(StatusOperacao.EM_SEPARACAO);
        separacao.setStartDate(now);
        separacao.setCompletionDate(null);
        separacao.setActive(true);
        setCreationAuditFields(separacao, separacao.getUserName(), now);
        entityManager.persist(separacao);
        entityManager.flush();
        auditService.record("CREATE", separacao, null, auditService.snapshot(separacao));
        return separacao;
    }

    @Transactional
    public Separacao concluir(Long id, Separacao alteracao) {
        Separacao separacao = entityManager.find(Separacao.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (separacao == null || !Boolean.TRUE.equals(separacao.getActive())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Separação não encontrada ou inativa.");
        }
        if (alteracao == null || alteracao.getStatus() != StatusOperacao.CONCLUIDO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A única transição permitida é concluir a separação.");
        }
        if (separacao.getStatus() != StatusOperacao.EM_SEPARACAO) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Somente uma separação em andamento pode ser concluída.");
        }
        String previousState = auditService.snapshot(separacao);
        LocalDateTime now = LocalDateTime.now();
        separacao.setStatus(StatusOperacao.CONCLUIDO);
        separacao.setCompletionDate(now);
        separacao.setUserNameLastChange(alteracao.getUserNameLastChange() == null
                || alteracao.getUserNameLastChange().isBlank()
                ? separacao.getUserName() : alteracao.getUserNameLastChange());
        separacao.setLastChangeDate(now);
        entityManager.flush();
        auditService.record("UPDATE", separacao, previousState, auditService.snapshot(separacao));
        return separacao;
    }

    @Transactional(readOnly = true)
    public void validarExclusao(Long id) {
        Long itemCount = entityManager.createQuery(
                        "select count(item) from SeparacaoItem item "
                                + "where item.separationId = :separationId and item.active = true",
                        Long.class)
                .setParameter("separationId", id)
                .getSingleResult();
        if (itemCount > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível excluir uma separação que já possui itens registrados.");
        }
    }

    @Transactional
    public SeparacaoItem registrarItem(SeparacaoItem item) {
        if (item == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O item da separação é obrigatório.");
        }
        Separacao separacao = item.getSeparationId() == null
                ? null : entityManager.find(
                        Separacao.class, item.getSeparationId(), LockModeType.PESSIMISTIC_WRITE);
        if (separacao == null || !Boolean.TRUE.equals(separacao.getActive())
                || separacao.getStatus() != StatusOperacao.EM_SEPARACAO) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A separação não existe, está inativa ou não está em andamento.");
        }
        if (item.getUserId() == null || item.getUserId() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O identificador do operador deve ser positivo.");
        }
        if (item.getLocationId() == null || item.getLocationId() <= 0
                || item.getQuantity() == null || item.getQuantity().signum() <= 0
                || (item.getWeight() != null && item.getWeight().signum() < 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Local e quantidade positiva são obrigatórios; o peso não pode ser negativo.");
        }

        PedidoItem pedidoItem = item.getOrderItemId() == null
                ? null : entityManager.find(PedidoItem.class, item.getOrderItemId(), LockModeType.PESSIMISTIC_WRITE);
        if (pedidoItem == null || !Boolean.TRUE.equals(pedidoItem.getActive())
                || !separacao.getOrderId().equals(pedidoItem.getOrderId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O item deve pertencer ao pedido associado à separação.");
        }
        validarQuantidadeDoPedido(item, pedidoItem);

        Selo selo = validarSelo(item, pedidoItem);
        Long batchId = selo == null ? null : selo.getBatchId();
        Long logisticUnitId = selo == null ? null : selo.getLogisticUnitId();
        String userName = separacao.getUserName();
        LocalDateTime now = LocalDateTime.now();

        item.setId(null);
        item.setActive(true);
        setCreationAuditFields(item, userName, now);
        entityManager.persist(item);
        entityManager.flush();

        String code = "SEPARACAO-" + separacao.getId() + "-" + item.getId();
        OrderMovementRequest request = new OrderMovementRequest(
                code,
                MovementType.SAIDA,
                pedidoItem.getProductId(),
                batchId,
                item.getLocationId(),
                logisticUnitId,
                item.getQuantity(),
                item.getWeight(),
                MotivoMovimentacao.SEPARACAO_PEDIDO,
                item.getUserId(),
                userName,
                "Separação " + separacao.getId() + ", item " + item.getId());
        orderMovementProcessor.deductStockForSeparation(request);
        registrarMovimentacao(request, item, separacao, selo, now);
        auditService.record("CREATE", item, null, auditService.snapshot(item));
        return item;
    }

    private void validarQuantidadeDoPedido(SeparacaoItem item, PedidoItem pedidoItem) {
        if (pedidoItem.getQuantity() == null) {
            throw new IllegalStateException("A quantidade solicitada do item do pedido não foi informada.");
        }
        BigDecimal jaSeparada = entityManager.createQuery(
                        "select coalesce(sum(item.quantity), :zero) from SeparacaoItem item "
                                + "where item.orderItemId = :orderItemId and item.active = true",
                        BigDecimal.class)
                .setParameter("orderItemId", pedidoItem.getId())
                .setParameter("zero", BigDecimal.ZERO)
                .getSingleResult();
        if (jaSeparada.add(item.getQuantity()).compareTo(pedidoItem.getQuantity()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A quantidade separada excede a quantidade solicitada no pedido.");
        }
    }

    private Selo validarSelo(SeparacaoItem item, PedidoItem pedidoItem) {
        if (item.getSealId() == null) {
            return null;
        }
        Selo selo = seloRepository.findById(item.getSealId())
                .filter(value -> Boolean.TRUE.equals(value.getActive()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "O selo não existe ou está inativo."));
        if (!pedidoItem.getProductId().equals(selo.getProductId())
                || !item.getLocationId().equals(selo.getLocationId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O selo não corresponde ao produto ou ao local do item separado.");
        }
        if (selo.getQuantityAvailable() == null
                || selo.getQuantityAvailable().compareTo(item.getQuantity()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O saldo disponível do selo é insuficiente.");
        }
        return selo;
    }

    private void registrarMovimentacao(
            OrderMovementRequest request,
            SeparacaoItem item,
            Separacao separacao,
            Selo selo,
            LocalDateTime timestamp) {
        MovimentacaoEstoque movement = new MovimentacaoEstoque();
        movement.setProductId(request.productId());
        movement.setBatchId(request.batchId());
        movement.setLogisticUnitId(request.logisticUnitId());
        movement.setMovementType(MovementType.SAIDA);
        movement.setOriginLocationId(request.locationId());
        movement.setQuantity(request.quantity());
        movement.setWeight(request.weight());
        movement.setMovementDate(timestamp);
        movement.setUserName(request.userName());
        setCreationAuditFields(movement, request.userName(), timestamp);
        MovimentacaoEstoque savedMovement = movementRepository.save(movement);

        TransactionLog transaction = new TransactionLog();
        transaction.setTransactionId(UUID.randomUUID().toString());
        transaction.setMovementId(savedMovement.getId());
        transaction.setUserId(request.userId());
        transaction.setOriginLocationId(request.locationId());
        transaction.setTransactionDate(timestamp);
        transaction.setSealId(selo == null ? null : selo.getId());
        transaction.setProductId(request.productId());
        transaction.setBatchId(request.batchId());
        transaction.setReason(request.reason().getDescricao());
        transaction.setMovementType(MovementType.SAIDA);
        transaction.setQuantity(request.quantity());
        transaction.setWeight(request.weight());
        transaction.setAdditionalData("{\"separationId\":" + separacao.getId()
                + ",\"separationItemId\":" + item.getId() + "}");
        setCreationAuditFields(transaction, request.userName(), timestamp);
        transactionStore.save(transaction);
    }

    private void setCreationAuditFields(BaseEntity entity, String userName, LocalDateTime timestamp) {
        entity.setUserNameCreator(userName);
        entity.setCreationDate(timestamp);
        entity.setUserNameLastChange(userName);
        entity.setLastChangeDate(timestamp);
    }
}
