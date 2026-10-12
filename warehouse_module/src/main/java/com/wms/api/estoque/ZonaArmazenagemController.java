package com.wms.api.estoque;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.estoque.ZonaArmazenagem;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/zonas-armazenagem")
@Tag(name = "Zonas de armazenagem", description = "Cadastro de zonas nos armazéns")
public class ZonaArmazenagemController extends BaseCrudController<ZonaArmazenagem> {
    public ZonaArmazenagemController(EntityManager entityManager) {
        super(entityManager, ZonaArmazenagem.class);
    }
}
