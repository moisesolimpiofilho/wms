package com.wms.domain.estoque;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "armazem")
@EqualsAndHashCode(callSuper = true)
public class Armazem extends BaseEntity {
    @Column(nullable = false) private Long centerDistributionId;
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private Boolean active;
}