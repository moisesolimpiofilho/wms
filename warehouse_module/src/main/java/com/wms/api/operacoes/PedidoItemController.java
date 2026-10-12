package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.itens.PedidoItem;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedido-itens")
@Tag(name = "Itens de pedido", description = "Itens vinculados aos pedidos")
public class PedidoItemController extends BaseCrudController<PedidoItem> {
    public PedidoItemController(EntityManager entityManager) {
        super(entityManager, PedidoItem.class);
    }
}
