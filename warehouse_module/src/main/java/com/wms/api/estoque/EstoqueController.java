package com.wms.api.estoque;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.estoque.Estoque;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/estoques")
@Tag(name = "Estoques", description = "Consulta e manutenção de saldos de estoque")
public class EstoqueController extends BaseCrudController<Estoque> {
    public EstoqueController(EntityManager entityManager) {
        super(entityManager, Estoque.class);
    }
}
