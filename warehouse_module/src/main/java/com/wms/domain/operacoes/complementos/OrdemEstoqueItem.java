package com.wms.domain.operacoes.complementos;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "ordem_estoque_item")
@EqualsAndHashCode(callSuper = true)
public class OrdemEstoqueItem extends BaseEntity {
    @Column(nullable = false) private Long orderId;
    @Column(nullable = false) private Long productId;
    @Column private Long batchId;
    @Column private Long sealId;
    @Column private Long logisticUnitId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantity;
    @Column(precision = 19, scale = 4) private BigDecimal weight;
    @Column(precision = 19, scale = 4) private BigDecimal executedQuantity;
    @Column(precision = 19, scale = 4) private BigDecimal executedWeight;
}