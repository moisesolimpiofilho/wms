package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.itens.PedidoItem;
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
@RequestMapping("/api/pedido-itens")
@Tag(name = "Itens de pedido", description = "Itens vinculados aos pedidos")
public class PedidoItemController extends BaseCrudController<PedidoItem> {
    public PedidoItemController(EntityManager entityManager) {
        super(entityManager, PedidoItem.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar itens de pedido", description = "Retorna todos os itens de pedido ativos.")
    @ApiResponse(responseCode = "200", description = "Itens listados com sucesso")
    public List<PedidoItem> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar item de pedido", description = "Busca um item de pedido ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")})
    public ResponseEntity<PedidoItem> buscarPorId(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar item de pedido", description = "Adiciona um item ao pedido.")
    @ApiResponse(responseCode = "201", description = "Item criado com sucesso")
    public ResponseEntity<PedidoItem> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do item", required = true)
            @RequestBody PedidoItem entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar item de pedido", description = "Atualiza um item de pedido ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Item atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")})
    public ResponseEntity<PedidoItem> atualizar(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do item", required = true)
            @RequestBody PedidoItem entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir item de pedido", description = "Desativa o item sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Item desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
