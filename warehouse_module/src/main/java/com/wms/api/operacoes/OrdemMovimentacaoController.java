package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.OrdemMovimentacao;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ordens-movimentacao")
@Tag(name = "Ordens de movimentação", description = "Gestão de movimentações entre locais")
public class OrdemMovimentacaoController extends BaseCrudController<OrdemMovimentacao> {
    public OrdemMovimentacaoController(EntityManager entityManager) {
        super(entityManager, OrdemMovimentacao.class);
    }
}
