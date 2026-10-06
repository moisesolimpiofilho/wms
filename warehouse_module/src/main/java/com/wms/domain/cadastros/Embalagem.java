package com.wms.domain.cadastros;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "embalagem")
@EqualsAndHashCode(callSuper = true)
public class Embalagem extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Long unitMeasureId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(precision = 19, scale = 4)
    private BigDecimal weight;

    @Column(nullable = false)
    private Boolean active; 
}