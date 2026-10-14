package com.wms.integration.estoque;

import com.wms.domain.enums.MovementType;

import java.math.BigDecimal;

public record OrderMovementRequest(
        String code,
        MovementType movementType,
        Long productId,
        Long batchId,
        Long locationId,
        Long logisticUnitId,
        BigDecimal quantity,
        BigDecimal weight,
        Long reasonId,
        Long userId,
        String userName,
        String observation) {

    public void validate() {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("O código da ordem é obrigatório");
        }
        if (movementType != MovementType.ENTRADA && movementType != MovementType.SAIDA) {
            throw new IllegalArgumentException("A ordem deve ser do tipo ENTRADA ou SAIDA");
        }
        if (productId == null || productId <= 0 || locationId == null || locationId <= 0) {
            throw new IllegalArgumentException("Produto e local devem ser informados");
        }
        if ((batchId != null && batchId <= 0) || (logisticUnitId != null && logisticUnitId <= 0)) {
            throw new IllegalArgumentException("Lote e unidade logística devem ser positivos");
        }
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("A quantidade deve ser maior que zero");
        }
        if (weight != null && weight.signum() < 0) {
            throw new IllegalArgumentException("O peso não pode ser negativo");
        }
        if (reasonId == null || reasonId <= 0 || userId == null || userId <= 0) {
            throw new IllegalArgumentException("Motivo e usuário devem ser informados");
        }
        if (userName == null || userName.isBlank()) {
            throw new IllegalArgumentException("O nome do usuário é obrigatório");
        }
    }
}
