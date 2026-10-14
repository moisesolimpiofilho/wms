package com.wms.domain.identificacao;

import com.wms.domain.base.BaseEntity;
import com.wms.domain.enums.MovementType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "movimentacao_estoque")
@EqualsAndHashCode(callSuper = true)
public class MovimentacaoEstoque extends BaseEntity {
    @Column(nullable = false) private Long productId;
    @Column private Long batchId;
    @Column private Long originLocationId;
    @Column private Long destinationLocationId;
    @Column private Long logisticUnitId;
    @Enumerated(EnumType.ORDINAL) @Column(nullable = false) private MovementType movementType;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantity;
    @Column(precision = 19, scale = 4) private BigDecimal weight;
    @Column(nullable = false) private LocalDateTime movementDate;
    @Column(nullable = false) private String userName;
}
