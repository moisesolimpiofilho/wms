package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.ajustes.MotivoMovimentacao;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/motivos-movimentacao")
@Tag(name = "Motivos de movimentação", description = "Cadastro dos motivos de movimentação de estoque")
public class MotivoMovimentacaoController extends BaseCrudController<MotivoMovimentacao> {
    public MotivoMovimentacaoController(EntityManager entityManager) {
        super(entityManager, MotivoMovimentacao.class);
    }
}
