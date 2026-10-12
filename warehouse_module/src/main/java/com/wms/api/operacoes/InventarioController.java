package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.Inventario;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventarios")
@Tag(name = "Inventários", description = "Gestão de inventários de armazém")
public class InventarioController extends BaseCrudController<Inventario> {
    public InventarioController(EntityManager entityManager) {
        super(entityManager, Inventario.class);
    }
}
