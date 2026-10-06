package com.wms.domain.operacoes;

import com.wms.domain.base.BaseEntity;
import com.wms.domain.enums.StatusOperacao;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "separacao")
@EqualsAndHashCode(callSuper = true)
public class Separacao extends BaseEntity {
    @Column(nullable = false) private Long orderId;
    @Enumerated(EnumType.ORDINAL) @Column(nullable = false) private StatusOperacao status;
    @Column private LocalDateTime startDate;
    @Column private LocalDateTime completionDate;
    @Column private String userName;
}