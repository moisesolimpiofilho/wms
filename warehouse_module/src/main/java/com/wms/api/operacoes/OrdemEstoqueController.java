package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.OrdemEstoque;
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
@RequestMapping("/api/ordens-estoque")
@Tag(name = "Ordens de estoque", description = "Gestão de ordens de movimentação de estoque")
public class OrdemEstoqueController extends BaseCrudController<OrdemEstoque> {
    public OrdemEstoqueController(EntityManager entityManager) {
        super(entityManager, OrdemEstoque.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar ordens de estoque", description = "Retorna todas as ordens de estoque ativas.")
    @ApiResponse(responseCode = "200", description = "Ordens listadas com sucesso")
    public List<OrdemEstoque> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar ordem de estoque", description = "Busca uma ordem ativa pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Ordem encontrada"),
            @ApiResponse(responseCode = "404", description = "Ordem não encontrada ou inativa")})
    public ResponseEntity<OrdemEstoque> buscarPorId(
            @Parameter(description = "ID da ordem", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar ordem de estoque", description = "Registra uma ordem de estoque.")
    @ApiResponse(responseCode = "201", description = "Ordem criada com sucesso")
    public ResponseEntity<OrdemEstoque> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados da ordem", required = true)
            @RequestBody OrdemEstoque entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar ordem de estoque", description = "Atualiza uma ordem de estoque ativa.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Ordem atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ordem não encontrada ou inativa")})
    public ResponseEntity<OrdemEstoque> atualizar(
            @Parameter(description = "ID da ordem", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados da ordem", required = true)
            @RequestBody OrdemEstoque entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir ordem de estoque", description = "Desativa a ordem sem removê-la do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Ordem desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Ordem não encontrada ou inativa")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da ordem", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
