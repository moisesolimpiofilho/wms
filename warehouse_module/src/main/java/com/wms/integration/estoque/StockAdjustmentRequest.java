package com.wms.integration.estoque;

import com.wms.domain.operacoes.ajustes.MotivoMovimentacao;

import java.math.BigDecimal;

public record StockAdjustmentRequest(
        Long productId,
        Long locationId,
        Long batchId,
        BigDecimal countedQuantity,
        BigDecimal countedWeight,
        MotivoMovimentacao reason,
        Long userId,
        String userName) {

    public void validate() {
        if (productId == null || productId <= 0 || locationId == null || locationId <= 0
                || (batchId != null && batchId <= 0)) {
            throw new IllegalArgumentException("Produto, local e lote devem ser válidos");
        }
        if (countedQuantity == null || countedQuantity.signum() < 0) {
            throw new IllegalArgumentException("A quantidade contada deve ser informada e não pode ser negativa");
        }
        if (countedWeight != null && countedWeight.signum() < 0) {
            throw new IllegalArgumentException("O peso contado não pode ser negativo");
        }
        if (reason != MotivoMovimentacao.AJUSTE_INVENTARIO) {
            throw new IllegalArgumentException("O motivo deve ser AJUSTE_INVENTARIO");
        }
        if (userId == null || userId <= 0 || userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("Usuário e nome do usuário devem ser informados");
        }
    }
}
