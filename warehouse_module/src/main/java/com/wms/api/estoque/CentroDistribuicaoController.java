package com.wms.api.estoque;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.estoque.CentroDistribuicao;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/centros-distribuicao")
@Tag(name = "Centros de distribuição", description = "Cadastro de centros de distribuição")
public class CentroDistribuicaoController extends BaseCrudController<CentroDistribuicao> {
    public CentroDistribuicaoController(EntityManager entityManager) {
        super(entityManager, CentroDistribuicao.class);
    }
}
