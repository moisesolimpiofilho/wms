package com.wms.service.estoque;

import com.wms.domain.enums.MovementType;
import com.wms.domain.operacoes.ajustes.MotivoMovimentacao;
import com.wms.domain.operacoes.itens.RecebimentoItem;
import com.wms.integration.estoque.OrderMovementRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecebimentoEstoqueService {
    private final OrderMovementProcessor orderMovementProcessor;

    public RecebimentoEstoqueService(OrderMovementProcessor orderMovementProcessor) {
        this.orderMovementProcessor = orderMovementProcessor;
    }

    @Transactional
    public void registrarEntrada(RecebimentoItem item) {
        if (item.getId() == null || item.getReceiptId() == null) {
            throw new IllegalArgumentException("O item e o recebimento devem estar persistidos");
        }
        if (item.getLocationId() == null) {
            throw new IllegalArgumentException("O local de destino é obrigatório");
        }
        if (item.getUserId() == null || item.getUserId() <= 0) {
            throw new IllegalArgumentException("O identificador do usuário deve ser positivo");
        }
        MotivoMovimentacao reason = item.getReason();
        if (reason != MotivoMovimentacao.RECEBIMENTO_AQUISICAO
                && reason != MotivoMovimentacao.RECEBIMENTO_PRODUCAO) {
            throw new IllegalArgumentException("O motivo deve corresponder a um recebimento");
        }
        String userName = item.getUserNameLastChange();
        if (userName == null || userName.isBlank()) {
            userName = item.getUserNameCreator();
        }
        String code = "RECEBIMENTO-" + item.getReceiptId() + "-" + item.getId();
        OrderMovementRequest request = new OrderMovementRequest(
                code,
                MovementType.ENTRADA,
                item.getProductId(),
                item.getBatchId(),
                item.getLocationId(),
                null,
                item.getQuantity(),
                item.getWeight(),
                reason,
                item.getUserId(),
                userName,
                "Entrada do item " + item.getId() + " do recebimento " + item.getReceiptId());
        orderMovementProcessor.process(request);
    }
}
