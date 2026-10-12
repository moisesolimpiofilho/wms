package com.wms.api.estoque;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.estoque.LocalEstoque;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/locais-estoque")
@Tag(name = "Locais de estoque", description = "Cadastro de posições de armazenagem")
public class LocalEstoqueController extends BaseCrudController<LocalEstoque> {
    public LocalEstoqueController(EntityManager entityManager) {
        super(entityManager, LocalEstoque.class);
    }
}
