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
@Table(name = "lote")
@EqualsAndHashCode(callSuper = true)
public class Lote extends BaseEntity {
    @Column(nullable = false) private Long productId;
    @Column(nullable = false) private String code;
    @Column private LocalDateTime manufacturingDate;
    @Column private LocalDateTime expirationDate;
    @Column(nullable = false) private Boolean active;
}