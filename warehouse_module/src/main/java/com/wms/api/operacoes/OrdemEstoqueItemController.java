package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.complementos.OrdemEstoqueItem;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ordens-estoque-itens")
@Tag(name = "Itens de ordem de estoque", description = "Itens vinculados às ordens de estoque")
public class OrdemEstoqueItemController extends BaseCrudController<OrdemEstoqueItem> {
    public OrdemEstoqueItemController(EntityManager entityManager) {
        super(entityManager, OrdemEstoqueItem.class);
    }
}
