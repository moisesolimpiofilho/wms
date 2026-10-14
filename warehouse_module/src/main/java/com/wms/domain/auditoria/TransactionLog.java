package com.wms.domain.auditoria;

import com.wms.domain.base.BaseEntity;
import com.wms.domain.enums.MovementType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transaction_log")
@EqualsAndHashCode(callSuper = true)
public class TransactionLog extends BaseEntity {

    @Column(nullable = false)
    private String transactionId;

    @Column(nullable = false)
    private Long movementId;

    @Column(nullable = false)
    private Long userId;

    @Column
    private Long originLocationId;

    @Column
    private Long destinationLocationId;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Column(nullable = false)
    private Long sealId;

    @Column(nullable = false)
    private Long productId;

    @Column
    private Long batchId;

    @Column(nullable = false)
    private String reason;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private MovementType movementType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(precision = 19, scale = 4)
    private BigDecimal weight;

    @Column(columnDefinition = "JSON")
    private String additionalData;
}