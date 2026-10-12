package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.Separacao;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/separacoes")
@Tag(name = "Separações", description = "Gestão de separação de pedidos")
public class SeparacaoController extends BaseCrudController<Separacao> {
    public SeparacaoController(EntityManager entityManager) {
        super(entityManager, Separacao.class);
    }
}
