package com.wms.api.identificacao;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.identificacao.Lote;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lotes")
@Tag(name = "Lotes", description = "Cadastro e consulta de lotes de produtos")
public class LoteController extends BaseCrudController<Lote> {
    public LoteController(EntityManager entityManager) {
        super(entityManager, Lote.class);
    }
}
