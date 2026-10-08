package com.wms.domain.operacoes;

import com.wms.domain.base.BaseEntity;
import com.wms.domain.enums.StatusOperacao;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "pedido")
@EqualsAndHashCode(callSuper = true)
public class Pedido extends BaseEntity {
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private Long customerId;
    @Enumerated(EnumType.ORDINAL) @Column(nullable = false) private StatusOperacao status;
    @Column(nullable = false) private LocalDateTime orderDate;
    @Column private LocalDateTime expectedShippingDate;
}