package com.wms.domain.cadastros;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "produto")
@EqualsAndHashCode(callSuper = true)
public class Produto extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Long categoryId;

    @Column
    private Long segmentId;

    @Column(nullable = false)
    private Long unitMeasureId;

    @Column(nullable = false)
    private Boolean active;

    @Column(nullable = false)
    private Boolean batchControl;
}