package com.wms.api.cadastros;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.cadastros.Cliente;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Cadastro de clientes")
public class ClienteController extends BaseCrudController<Cliente> {
    public ClienteController(EntityManager entityManager) {
        super(entityManager, Cliente.class);
    }
}
