package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.itens.RecebimentoItem;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recebimento-itens")
@Tag(name = "Itens de recebimento", description = "Itens vinculados aos recebimentos")
public class RecebimentoItemController extends BaseCrudController<RecebimentoItem> {
    public RecebimentoItemController(EntityManager entityManager) {
        super(entityManager, RecebimentoItem.class);
    }
}
