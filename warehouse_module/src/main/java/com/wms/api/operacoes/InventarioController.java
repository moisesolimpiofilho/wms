package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.Inventario;
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
@RequestMapping("/api/inventarios")
@Tag(name = "Inventários", description = "Gestão de inventários de armazém")
public class InventarioController extends BaseCrudController<Inventario> {
    public InventarioController(EntityManager entityManager) {
        super(entityManager, Inventario.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar inventários", description = "Retorna todos os inventários ativos.")
    @ApiResponse(responseCode = "200", description = "Inventários listados com sucesso")
    public List<Inventario> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar inventário", description = "Busca um inventário ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Inventário encontrado"),
            @ApiResponse(responseCode = "404", description = "Inventário não encontrado ou inativo")})
    public ResponseEntity<Inventario> buscarPorId(
            @Parameter(description = "ID do inventário", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar inventário", description = "Abre um inventário de armazém.")
    @ApiResponse(responseCode = "201", description = "Inventário criado com sucesso")
    public ResponseEntity<Inventario> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do inventário", required = true)
            @RequestBody Inventario entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar inventário", description = "Atualiza um inventário ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Inventário atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Inventário não encontrado ou inativo")})
    public ResponseEntity<Inventario> atualizar(
            @Parameter(description = "ID do inventário", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do inventário", required = true)
            @RequestBody Inventario entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir inventário", description = "Desativa o inventário sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Inventário desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Inventário não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do inventário", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
