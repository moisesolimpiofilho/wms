package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.ajustes.MotivoMovimentacao;
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
@RequestMapping("/api/motivos-movimentacao")
@Tag(name = "Motivos de movimentação", description = "Cadastro dos motivos de movimentação de estoque")
public class MotivoMovimentacaoController extends BaseCrudController<MotivoMovimentacao> {
    public MotivoMovimentacaoController(EntityManager entityManager) {
        super(entityManager, MotivoMovimentacao.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar motivos de movimentação", description = "Retorna todos os motivos ativos.")
    @ApiResponse(responseCode = "200", description = "Motivos listados com sucesso")
    public List<MotivoMovimentacao> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar motivo de movimentação", description = "Busca um motivo ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Motivo encontrado"),
            @ApiResponse(responseCode = "404", description = "Motivo não encontrado ou inativo")})
    public ResponseEntity<MotivoMovimentacao> buscarPorId(
            @Parameter(description = "ID do motivo", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar motivo de movimentação", description = "Cadastra um motivo de movimentação.")
    @ApiResponse(responseCode = "201", description = "Motivo criado com sucesso")
    public ResponseEntity<MotivoMovimentacao> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do motivo", required = true)
            @RequestBody MotivoMovimentacao entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar motivo de movimentação", description = "Atualiza um motivo ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Motivo atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Motivo não encontrado ou inativo")})
    public ResponseEntity<MotivoMovimentacao> atualizar(
            @Parameter(description = "ID do motivo", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do motivo", required = true)
            @RequestBody MotivoMovimentacao entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir motivo de movimentação", description = "Desativa o motivo sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Motivo desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Motivo não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do motivo", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
