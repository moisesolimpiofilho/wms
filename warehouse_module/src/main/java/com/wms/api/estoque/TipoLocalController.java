package com.wms.api.estoque;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.estoque.TipoLocal;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tipos-local")
@Tag(name = "Tipos de local", description = "Cadastro de tipos de posição de armazenagem")
public class TipoLocalController extends BaseCrudController<TipoLocal> {
    public TipoLocalController(EntityManager entityManager) {
        super(entityManager, TipoLocal.class);
    }
}
