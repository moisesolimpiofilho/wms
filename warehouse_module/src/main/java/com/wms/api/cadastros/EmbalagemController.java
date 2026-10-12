package com.wms.api.cadastros;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.cadastros.Embalagem;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/embalagens")
@Tag(name = "Embalagens", description = "Cadastro de embalagens e suas unidades")
public class EmbalagemController extends BaseCrudController<Embalagem> {
    public EmbalagemController(EntityManager entityManager) {
        super(entityManager, Embalagem.class);
    }
}
