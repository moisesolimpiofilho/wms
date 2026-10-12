package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.Expedicao;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expedicoes")
@Tag(name = "Expedições", description = "Gestão das expedições de pedidos")
public class ExpedicaoController extends BaseCrudController<Expedicao> {
    public ExpedicaoController(EntityManager entityManager) {
        super(entityManager, Expedicao.class);
    }
}
