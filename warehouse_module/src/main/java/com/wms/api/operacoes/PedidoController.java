package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.Pedido;
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
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Gestão de pedidos de clientes")
public class PedidoController extends BaseCrudController<Pedido> {
    public PedidoController(EntityManager entityManager) {
        super(entityManager, Pedido.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar pedidos", description = "Retorna todos os pedidos ativos.")
    @ApiResponse(responseCode = "200", description = "Pedidos listados com sucesso")
    public List<Pedido> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar pedido", description = "Busca um pedido ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Pedido encontrado"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado ou inativo")})
    public ResponseEntity<Pedido> buscarPorId(
            @Parameter(description = "ID do pedido", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar pedido", description = "Registra um pedido de cliente.")
    @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso")
    public ResponseEntity<Pedido> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do pedido", required = true)
            @RequestBody Pedido entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar pedido", description = "Atualiza um pedido ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Pedido atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado ou inativo")})
    public ResponseEntity<Pedido> atualizar(
            @Parameter(description = "ID do pedido", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do pedido", required = true)
            @RequestBody Pedido entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir pedido", description = "Desativa o pedido sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Pedido desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Pedido não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do pedido", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
