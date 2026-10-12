package com.wms.api.identificacao;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.identificacao.Selo;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/selos")
@Tag(name = "Selos", description = "Cadastro e consulta de etiquetas de estoque")
public class SeloController extends BaseCrudController<Selo> {
    public SeloController(EntityManager entityManager) {
        super(entityManager, Selo.class);
    }
}
