package com.wms.api.estoque;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.estoque.Armazem;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/armazens")
@Tag(name = "Armazéns", description = "Cadastro de armazéns")
public class ArmazemController extends BaseCrudController<Armazem> {
    public ArmazemController(EntityManager entityManager) {
        super(entityManager, Armazem.class);
    }
}
