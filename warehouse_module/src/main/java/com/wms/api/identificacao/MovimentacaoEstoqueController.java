package com.wms.api.identificacao;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.identificacao.MovimentacaoEstoque;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/movimentacoes-estoque")
@Tag(name = "Movimentações de estoque", description = "Registro e consulta de movimentações")
public class MovimentacaoEstoqueController extends BaseCrudController<MovimentacaoEstoque> {
    public MovimentacaoEstoqueController(EntityManager entityManager) {
        super(entityManager, MovimentacaoEstoque.class);
    }
}
