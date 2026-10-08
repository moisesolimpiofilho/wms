package com.wms.domain.operacoes;

import com.wms.domain.base.BaseEntity;
import com.wms.domain.enums.StatusOperacao;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "recebimento")
@EqualsAndHashCode(callSuper = true)
public class Recebimento extends BaseEntity {
    @Column(nullable = false, unique = true) private String code;
    @Column private Long supplierId;
    @Enumerated(EnumType.ORDINAL) @Column(nullable = false) private StatusOperacao status;
    @Column private LocalDateTime receiptDate;
    @Column private String observation;
}