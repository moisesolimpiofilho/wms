package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.Recebimento;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recebimentos")
@Tag(name = "Recebimentos", description = "Gestão de recebimentos de fornecedores")
public class RecebimentoController extends BaseCrudController<Recebimento> {
    public RecebimentoController(EntityManager entityManager) {
        super(entityManager, Recebimento.class);
    }
}
