package com.wms.domain.operacoes;

import com.wms.domain.base.BaseEntity;
import com.wms.domain.enums.StatusOperacao;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "inventario")
@EqualsAndHashCode(callSuper = true)
public class Inventario extends BaseEntity {
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private Long warehouseId;
    @Enumerated(EnumType.ORDINAL) @Column(nullable = false) private StatusOperacao status;
    @Column private LocalDateTime startDate;
    @Column private LocalDateTime completionDate;
    @Column private String userName;
}
