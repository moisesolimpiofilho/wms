package com.wms.domain.cadastros;

import com.wms.domain.base.BaseEntity;
import com.wms.domain.validation.BrazilianDocument;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "cliente")
@EqualsAndHashCode(callSuper = true)
public class Cliente extends BaseEntity {
    @Column(nullable = false, unique = true) private String code;
    @Column(nullable = false) private String name;
    @Column(unique = true) private String document; // CNPJ ou CPF

    @PrePersist
    @PreUpdate
    private void normalizeDocument() {
        document = BrazilianDocument.normalize(document);
    }

}