package com.wms.domain.operacoes;

import com.wms.domain.base.BaseEntity;
import com.wms.domain.enums.StatusOperacao;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "expedicao")
@EqualsAndHashCode(callSuper = true)
public class Expedicao extends BaseEntity {
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private Long orderId;
    @Enumerated(EnumType.ORDINAL) @Column(nullable = false) private StatusOperacao status;
    @Column private LocalDateTime shippingDate;
    @Column private String vehiclePlate;
}

