package com.wms.domain.operacoes.itens;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "recebimento_item")
@EqualsAndHashCode(callSuper = true)
public class RecebimentoItem extends BaseEntity {
    @Column(nullable = false) private Long receiptId;
    @Column(nullable = false) private Long productId;
    @Column private Long batchId;
    @Column(nullable = false, precision = 19, scale = 4) private BigDecimal quantity;
    @Column(precision = 19, scale = 4) private BigDecimal weight;
}
