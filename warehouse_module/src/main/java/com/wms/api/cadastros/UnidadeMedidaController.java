package com.wms.api.cadastros;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.cadastros.UnidadeMedida;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/unidades-medida")
@Tag(name = "Unidades de medida", description = "Cadastro de unidades de medida")
public class UnidadeMedidaController extends BaseCrudController<UnidadeMedida> {
    public UnidadeMedidaController(EntityManager entityManager) {
        super(entityManager, UnidadeMedida.class);
    }
}
