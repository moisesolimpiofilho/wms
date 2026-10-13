package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.OrdemMovimentacao;
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
@RequestMapping("/api/ordens-movimentacao")
@Tag(name = "Ordens de movimentação", description = "Gestão de movimentações entre locais")
public class OrdemMovimentacaoController extends BaseCrudController<OrdemMovimentacao> {
    public OrdemMovimentacaoController(EntityManager entityManager) {
        super(entityManager, OrdemMovimentacao.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar ordens de movimentação", description = "Retorna todas as ordens ativas.")
    @ApiResponse(responseCode = "200", description = "Ordens listadas com sucesso")
    public List<OrdemMovimentacao> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar ordem de movimentação", description = "Busca uma ordem ativa pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Ordem encontrada"),
            @ApiResponse(responseCode = "404", description = "Ordem não encontrada ou inativa")})
    public ResponseEntity<OrdemMovimentacao> buscarPorId(
            @Parameter(description = "ID da ordem", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar ordem de movimentação", description = "Solicita uma movimentação entre locais.")
    @ApiResponse(responseCode = "201", description = "Ordem criada com sucesso")
    public ResponseEntity<OrdemMovimentacao> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados da ordem", required = true)
            @RequestBody OrdemMovimentacao entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar ordem de movimentação", description = "Atualiza uma ordem ativa.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Ordem atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ordem não encontrada ou inativa")})
    public ResponseEntity<OrdemMovimentacao> atualizar(
            @Parameter(description = "ID da ordem", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados da ordem", required = true)
            @RequestBody OrdemMovimentacao entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir ordem de movimentação", description = "Desativa a ordem sem removê-la do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Ordem desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ordem não encontrada ou inativa")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da ordem", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
