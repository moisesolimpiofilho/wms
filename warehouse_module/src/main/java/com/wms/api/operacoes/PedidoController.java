package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.Pedido;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Gestão de pedidos de clientes")
public class PedidoController extends BaseCrudController<Pedido> {
    public PedidoController(EntityManager entityManager) {
        super(entityManager, Pedido.class);
    }
}
