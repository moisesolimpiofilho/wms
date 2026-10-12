package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.itens.SeparacaoItem;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/separacao-itens")
@Tag(name = "Itens de separação", description = "Itens vinculados às separações de pedidos")
public class SeparacaoItemController extends BaseCrudController<SeparacaoItem> {
    public SeparacaoItemController(EntityManager entityManager) {
        super(entityManager, SeparacaoItem.class);
    }
}
