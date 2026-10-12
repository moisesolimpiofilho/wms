package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.itens.InventarioItem;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventario-itens")
@Tag(name = "Itens de inventário", description = "Itens e contagens vinculados a inventários")
public class InventarioItemController extends BaseCrudController<InventarioItem> {
    public InventarioItemController(EntityManager entityManager) {
        super(entityManager, InventarioItem.class);
    }
}
