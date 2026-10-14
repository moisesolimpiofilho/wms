package com.wms.api.identificacao;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.identificacao.MovimentacaoEstoque;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movimentacoes-estoque")
@Tag(name = "Movimentações de estoque", description = "Registro e consulta de movimentações")
public class MovimentacaoEstoqueController extends BaseCrudController<MovimentacaoEstoque> {
    public MovimentacaoEstoqueController(EntityManager entityManager) {
        super(entityManager, MovimentacaoEstoque.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar movimentações", description = "Retorna as movimentações de estoque ativas.")
    @ApiResponse(responseCode = "200", description = "Movimentações listadas com sucesso")
    public List<MovimentacaoEstoque> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar movimentação", description = "Busca uma movimentação ativa pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Movimentação encontrada"),
            @ApiResponse(responseCode = "404", description = "Movimentação não encontrada ou inativa")})
    public ResponseEntity<MovimentacaoEstoque> buscarPorId(
            @Parameter(description = "ID da movimentação", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar movimentação", description = "Registra uma movimentação de estoque.")
    @ApiResponse(responseCode = "201", description = "Movimentação criada com sucesso")
    public ResponseEntity<MovimentacaoEstoque> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados da movimentação", required = true)
            @RequestBody MovimentacaoEstoque entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar movimentação", description = "Atualiza uma movimentação ativa.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Movimentação atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Movimentação não encontrada ou inativa")})
    public ResponseEntity<MovimentacaoEstoque> atualizar(
            @Parameter(description = "ID da movimentação", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados da movimentação", required = true)
            @RequestBody MovimentacaoEstoque entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir movimentação", description = "Desativa a movimentação sem removê-la do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Movimentação desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Movimentação não encontrada ou inativa")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da movimentação", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
