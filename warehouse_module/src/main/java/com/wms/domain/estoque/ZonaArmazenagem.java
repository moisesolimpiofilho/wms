package com.wms.domain.estoque;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "zona_armazenagem")
@EqualsAndHashCode(callSuper = true)
public class ZonaArmazenagem extends BaseEntity {
    @Column(nullable = false) private Long warehouseId;
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private String description;
}
