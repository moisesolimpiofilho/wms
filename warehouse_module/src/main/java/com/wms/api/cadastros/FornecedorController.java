package com.wms.api.cadastros;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.cadastros.Fornecedor;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fornecedores")
@Tag(name = "Fornecedores", description = "Cadastro de fornecedores")
public class FornecedorController extends BaseCrudController<Fornecedor> {
    public FornecedorController(EntityManager entityManager) {
        super(entityManager, Fornecedor.class);
    }
}
