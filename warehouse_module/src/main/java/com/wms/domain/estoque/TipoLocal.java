package com.wms.domain.estoque;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "tipo_local")
@EqualsAndHashCode(callSuper = true)
public class TipoLocal extends BaseEntity {
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private Boolean active;
}
