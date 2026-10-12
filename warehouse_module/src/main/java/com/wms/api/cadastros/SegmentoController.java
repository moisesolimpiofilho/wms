package com.wms.api.cadastros;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.cadastros.Segmento;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/segmentos")
@Tag(name = "Segmentos", description = "Cadastro de segmentos de produtos")
public class SegmentoController extends BaseCrudController<Segmento> {
    public SegmentoController(EntityManager entityManager) {
        super(entityManager, Segmento.class);
    }
}
