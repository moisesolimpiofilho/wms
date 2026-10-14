package com.wms.domain.operacoes.itens;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "inventario_item")
@EqualsAndHashCode(callSuper = true)
public class InventarioItem extends BaseEntity {
    @Column(nullable = false) private Long inventoryId;
    @Column(nullable = false) private Long productId;
    @Column private Long batchId;
    @Column(nullable = false) private Long locationId;
    @Column private Long sealId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantitySystem;
    @Column(precision = 19, scale = 4) private BigDecimal quantityCounted;
    @Column(precision = 19, scale = 4) private BigDecimal weightSystem;
    @Column(precision = 19, scale = 4) private BigDecimal weightCounted;
}