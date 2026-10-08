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
@Table(name = "unidade_logistica")
@EqualsAndHashCode(callSuper = true)
public class UnidadeLogistica extends BaseEntity {
    @Column(nullable = false, unique = true) private String code;
    @Column private Long packagingId;
    @Column private Long parentUnitId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantity;
    @Column(precision = 19, scale = 4) private BigDecimal weight;
    @Column(nullable = false) private Boolean active;
}
