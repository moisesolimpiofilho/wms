package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.itens.SeparacaoItem;
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
@RequestMapping("/api/separacao-itens")
@Tag(name = "Itens de separação", description = "Itens vinculados às separações de pedidos")
public class SeparacaoItemController extends BaseCrudController<SeparacaoItem> {
    public SeparacaoItemController(EntityManager entityManager) {
        super(entityManager, SeparacaoItem.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar itens de separação", description = "Retorna todos os itens de separação ativos.")
    @ApiResponse(responseCode = "200", description = "Itens listados com sucesso")
    public List<SeparacaoItem> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar item de separação", description = "Busca um item de separação ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")})
    public ResponseEntity<SeparacaoItem> buscarPorId(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar item de separação", description = "Adiciona um item à separação.")
    @ApiResponse(responseCode = "201", description = "Item criado com sucesso")
    public ResponseEntity<SeparacaoItem> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do item", required = true)
            @RequestBody SeparacaoItem entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar item de separação", description = "Atualiza um item de separação ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Item atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")})
    public ResponseEntity<SeparacaoItem> atualizar(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do item", required = true)
            @RequestBody SeparacaoItem entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir item de separação", description = "Desativa o item sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Item desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
