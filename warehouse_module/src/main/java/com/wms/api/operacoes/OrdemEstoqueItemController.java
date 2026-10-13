package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.complementos.OrdemEstoqueItem;
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
@RequestMapping("/api/ordens-estoque-itens")
@Tag(name = "Itens de ordem de estoque", description = "Itens vinculados às ordens de estoque")
public class OrdemEstoqueItemController extends BaseCrudController<OrdemEstoqueItem> {
    public OrdemEstoqueItemController(EntityManager entityManager) {
        super(entityManager, OrdemEstoqueItem.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar itens de ordem de estoque", description = "Retorna os itens ativos das ordens de estoque.")
    @ApiResponse(responseCode = "200", description = "Itens listados com sucesso")
    public List<OrdemEstoqueItem> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar item de ordem de estoque", description = "Busca um item ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")})
    public ResponseEntity<OrdemEstoqueItem> buscarPorId(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar item de ordem de estoque", description = "Adiciona um item a uma ordem de estoque.")
    @ApiResponse(responseCode = "201", description = "Item criado com sucesso")
    public ResponseEntity<OrdemEstoqueItem> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do item", required = true)
            @RequestBody OrdemEstoqueItem entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar item de ordem de estoque", description = "Atualiza um item ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Item atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")})
    public ResponseEntity<OrdemEstoqueItem> atualizar(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do item", required = true)
            @RequestBody OrdemEstoqueItem entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir item de ordem de estoque", description = "Desativa o item sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Item desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
