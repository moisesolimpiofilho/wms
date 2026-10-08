package com.wms.domain.operacoes;

import com.wms.domain.base.BaseEntity;
import com.wms.domain.enums.MovementType;
import com.wms.domain.enums.StatusOperacao;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "ordem_estoque")
@EqualsAndHashCode(callSuper = true)
public class OrdemEstoque extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private MovementType orderType;

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    private StatusOperacao status;

    @Column(nullable = false)
    private Long reasonId;

    @Column
    private Long originWarehouseId;

    @Column
    private Long destinationWarehouseId;

    @Column
    private Long originLocationId;

    @Column
    private Long destinationLocationId;

    @Column(nullable = false)
    private LocalDateTime requestDate;

    @Column
    private LocalDateTime executionDate;

    @Column(nullable = false)
    private Long userId;

    @Column
    private Long executionUserId;

    @Column
    private String observation; 
}