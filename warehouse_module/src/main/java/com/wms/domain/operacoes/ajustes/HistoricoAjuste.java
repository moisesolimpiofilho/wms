package com.wms.domain.operacoes.ajustes;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "historico_ajuste")
@EqualsAndHashCode(callSuper = true)
public class HistoricoAjuste extends BaseEntity {
    @Column(nullable = false)
    private Long inventoryId;

    @Column(nullable = false)
    private Long inventoryItemId;

    @Column(nullable = false)
    private Long movementId;

    @Column(nullable = false)
    private Long productId;

    @Column
    private Long batchId;

    @Column(nullable = false)
    private Long locationId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal systemQuantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal countedQuantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal differenceQuantity;

    @Column(precision = 19, scale = 4)
    private BigDecimal systemWeight;

    @Column(precision = 19, scale = 4)
    private BigDecimal countedWeight;

    @Column(precision = 19, scale = 4)
    private BigDecimal differenceWeight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAjuste adjustmentType;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private LocalDateTime adjustmentDate;
}
