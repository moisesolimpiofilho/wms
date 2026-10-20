package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.itens.SeparacaoItem;
import com.wms.service.operacoes.SeparacaoService;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/separacao-itens")
@Tag(name = "Itens de separação", description = "Itens vinculados às separações de pedidos")
public class SeparacaoItemController extends BaseCrudController<SeparacaoItem> {
    private final SeparacaoService separacaoService;

    public SeparacaoItemController(EntityManager entityManager, SeparacaoService separacaoService) {
        super(entityManager, SeparacaoItem.class);
        this.separacaoService = separacaoService;
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
    @Operation(summary = "Registrar item separado",
            description = "Valida pedido, selo e saldo, debita o estoque e atualiza o cache.")
    @ApiResponse(responseCode = "201", description = "Item criado com sucesso")
    public ResponseEntity<SeparacaoItem> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do item", required = true)
            @RequestBody SeparacaoItem entity) {
        SeparacaoItem item = separacaoService.registrarItem(entity);
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(item.getId())
                        .toUri())
                .body(item);
    }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Alterar item de separação",
            description = "Alterações de itens registrados são bloqueadas para preservar o histórico de estoque.")
    @ApiResponse(responseCode = "405", description = "Itens registrados são imutáveis")
    public ResponseEntity<SeparacaoItem> atualizar(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do item", required = true)
            @RequestBody SeparacaoItem entity) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                "Itens de separação registrados não podem ser alterados; registre o processo correto de ajuste.");
    }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir item de separação", description = "Itens registrados não podem ser removidos.")
    @ApiResponse(responseCode = "405", description = "Itens registrados não podem ser removidos")
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id) {
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED,
                "Itens de separação registrados não podem ser removidos.");
    }
}
