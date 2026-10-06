package com.wms.domain.operacoes.itens;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "pedido_item")
@EqualsAndHashCode(callSuper = true)
public class PedidoItem extends BaseEntity {
    @Column(nullable = false) private Long orderId;
    @Column(nullable = false) private Long productId;
    @Column private Long packagingId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantity;
    @Column(precision = 19, scale = 4) private BigDecimal weight;
}