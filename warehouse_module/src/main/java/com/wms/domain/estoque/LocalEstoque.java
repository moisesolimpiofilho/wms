package com.wms.domain.estoque;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "local_estoque")
@EqualsAndHashCode(callSuper = true)
public class LocalEstoque extends BaseEntity {

    @Column(nullable = false)
    private Long zoneId;

    @Column(nullable = false)
    private Long locationTypeId;

    @Column(nullable = false, unique = true)
    private String code;

    @Column
    private String description;

    @Column(precision = 19, scale = 4)
    private BigDecimal capacity;

    @Column(nullable = false)
    private Boolean active;
}