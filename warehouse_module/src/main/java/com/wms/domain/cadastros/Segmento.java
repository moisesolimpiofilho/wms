package com.wms.domain.cadastros;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "segmento")
@EqualsAndHashCode(callSuper = true)
public class Segmento extends BaseEntity {
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private Boolean active;
}
