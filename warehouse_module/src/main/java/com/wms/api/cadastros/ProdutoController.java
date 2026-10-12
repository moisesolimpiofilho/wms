package com.wms.api.cadastros;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.cadastros.Produto;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "Cadastro de produtos")
public class ProdutoController extends BaseCrudController<Produto> {
    public ProdutoController(EntityManager entityManager) {
        super(entityManager, Produto.class);
    }
}
