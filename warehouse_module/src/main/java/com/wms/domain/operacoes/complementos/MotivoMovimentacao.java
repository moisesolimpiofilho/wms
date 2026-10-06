package com.wms.domain.operacoes.ajustes;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "motivo_movimentacao")
@EqualsAndHashCode(callSuper = true)
public class MotivoMovimentacao extends BaseEntity {
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private Boolean active;
}
