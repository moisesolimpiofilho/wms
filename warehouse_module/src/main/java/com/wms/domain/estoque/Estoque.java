package com.wms.domain.estoque;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "estoque")
@EqualsAndHashCode(callSuper = true)
public class Estoque extends BaseEntity {

    @Column(nullable = false)
    private Long productId;

    @Column
    private Long batchId;

    @Column(nullable = false)
    private Long locationId;

    @Column
    private Long logisticUnitId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityReserved;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityAvailable;

    @Column(precision = 19, scale = 4)
    private BigDecimal weight;
}