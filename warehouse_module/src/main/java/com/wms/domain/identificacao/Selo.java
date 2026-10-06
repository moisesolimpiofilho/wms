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
@Table(name = "selo")
@EqualsAndHashCode(callSuper = true)
public class Selo extends BaseEntity {
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private Long productId;
    @Column private Long batchId;
    @Column private Long locationId;
    @Column private Long logisticUnitId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantity;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantityReserved;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantityCanceled;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantityAvailable;
    @Column(precision = 19, scale = 4) private BigDecimal weight;
    @Column(nullable = false) private Boolean active;
}

