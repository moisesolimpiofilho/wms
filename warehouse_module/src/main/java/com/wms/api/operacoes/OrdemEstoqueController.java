package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.OrdemEstoque;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ordens-estoque")
@Tag(name = "Ordens de estoque", description = "Gestão de ordens de movimentação de estoque")
public class OrdemEstoqueController extends BaseCrudController<OrdemEstoque> {
    public OrdemEstoqueController(EntityManager entityManager) {
        super(entityManager, OrdemEstoque.class);
    }
}
