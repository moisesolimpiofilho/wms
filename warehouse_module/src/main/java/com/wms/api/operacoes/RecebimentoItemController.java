package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.itens.RecebimentoItem;
import com.wms.service.estoque.RecebimentoEstoqueService;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/recebimento-itens")
@Tag(name = "Itens de recebimento", description = "Itens vinculados aos recebimentos")
public class RecebimentoItemController extends BaseCrudController<RecebimentoItem> {
    private final RecebimentoEstoqueService recebimentoEstoqueService;

    public RecebimentoItemController(
            EntityManager entityManager,
            RecebimentoEstoqueService recebimentoEstoqueService) {
        super(entityManager, RecebimentoItem.class);
        this.recebimentoEstoqueService = recebimentoEstoqueService;
    }

    @Override @GetMapping
    @Operation(summary = "Listar itens de recebimento", description = "Retorna todos os itens de recebimento ativos.")
    @ApiResponse(responseCode = "200", description = "Itens listados com sucesso")
    public List<RecebimentoItem> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar item de recebimento", description = "Busca um item ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")})
    public ResponseEntity<RecebimentoItem> buscarPorId(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar item de recebimento",
            description = "Adiciona o item e processa a entrada completa no estoque.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Item e entrada de estoque criados com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos para a entrada")
    })
    @Transactional
    public ResponseEntity<RecebimentoItem> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do item", required = true)
            @RequestBody RecebimentoItem entity) {
        ResponseEntity<RecebimentoItem> response = super.criar(entity);
        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            try {
                recebimentoEstoqueService.registrarEntrada(response.getBody());
            } catch (IllegalArgumentException exception) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
            }
        }
        return response;
    }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar item de recebimento", description = "Atualiza um item de recebimento ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Item atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")})
    public ResponseEntity<RecebimentoItem> atualizar(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do item", required = true)
            @RequestBody RecebimentoItem entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir item de recebimento", description = "Desativa o item sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Item desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
