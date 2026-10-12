package com.wms.api.cadastros;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.cadastros.Categoria;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Cadastro de categorias de produtos")
public class CategoriaController extends BaseCrudController<Categoria> {
    public CategoriaController(EntityManager entityManager) {
        super(entityManager, Categoria.class);
    }
}
