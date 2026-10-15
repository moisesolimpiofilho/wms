package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.itens.InventarioItem;
import com.wms.integration.estoque.StockAdjustmentRequest;
import com.wms.service.estoque.StockAdjustmentService;
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
@RequestMapping("/api/inventario-itens")
@Tag(name = "Itens de inventário", description = "Itens e contagens vinculados a inventários")
public class InventarioItemController extends BaseCrudController<InventarioItem> {
    private final StockAdjustmentService stockAdjustmentService;

    public InventarioItemController(EntityManager entityManager, StockAdjustmentService stockAdjustmentService) {
        super(entityManager, InventarioItem.class);
        this.stockAdjustmentService = stockAdjustmentService;
    }

    @PostMapping("/{id}/ajustes")
    @Operation(summary = "Aplicar ajuste de inventário",
            description = "Compara a contagem com o saldo atual e registra a divergência no estoque.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ajuste aplicado ou estoque já conciliado"),
            @ApiResponse(responseCode = "400", description = "Dados do ajuste inválidos"),
            @ApiResponse(responseCode = "404", description = "Item ou inventário não encontrado"),
            @ApiResponse(responseCode = "409", description = "Inventário encerrado ou saldo reservado incompatível")
    })
    public ResponseEntity<StockAdjustmentService.AdjustmentResult> ajustar(
            @Parameter(description = "ID do item de inventário", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Contagem física e dados do usuário responsável", required = true)
            @RequestBody StockAdjustmentRequest request) {
        return ResponseEntity.ok(stockAdjustmentService.adjust(id, request));
    }

    @Override @GetMapping
    @Operation(summary = "Listar itens de inventário", description = "Retorna os itens de inventário ativos.")
    @ApiResponse(responseCode = "200", description = "Itens listados com sucesso")
    public List<InventarioItem> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar item de inventário", description = "Busca um item ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Item encontrado"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")})
    public ResponseEntity<InventarioItem> buscarPorId(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar item de inventário", description = "Adiciona um item ao inventário.")
    @ApiResponse(responseCode = "201", description = "Item criado com sucesso")
    public ResponseEntity<InventarioItem> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do item", required = true)
            @RequestBody InventarioItem entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar item de inventário", description = "Atualiza um item de inventário ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Item atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")})
    public ResponseEntity<InventarioItem> atualizar(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do item", required = true)
            @RequestBody InventarioItem entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir item de inventário", description = "Desativa o item sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Item desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Item não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do item", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
