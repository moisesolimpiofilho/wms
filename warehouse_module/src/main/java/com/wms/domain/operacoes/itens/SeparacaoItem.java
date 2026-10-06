package com.wms.domain.operacoes.itens;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "separacao_item")
@EqualsAndHashCode(callSuper = true)
public class SeparacaoItem extends BaseEntity {
    @Column(nullable = false) private Long separationId;
    @Column(nullable = false) private Long orderItemId;
    @Column private Long sealId;
    @Column(nullable = false) private Long locationId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantity;
    @Column(precision = 19, scale = 4) private BigDecimal weight;
}