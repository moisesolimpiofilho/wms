package com.wms.api.estoque;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.estoque.Estoque;
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
@RequestMapping("/api/estoques")
@Tag(name = "Estoques", description = "Consulta e manutenção de saldos de estoque")
public class EstoqueController extends BaseCrudController<Estoque> {
    public EstoqueController(EntityManager entityManager) {
        super(entityManager, Estoque.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar estoques", description = "Retorna todos os registros de estoque ativos.")
    @ApiResponse(responseCode = "200", description = "Estoques listados com sucesso")
    public List<Estoque> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar estoque", description = "Busca um registro de estoque ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Estoque encontrado"),
            @ApiResponse(responseCode = "404", description = "Estoque não encontrado ou inativo")})
    public ResponseEntity<Estoque> buscarPorId(
            @Parameter(description = "ID do registro de estoque", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar registro de estoque", description = "Cria um registro de estoque.")
    @ApiResponse(responseCode = "201", description = "Registro criado com sucesso")
    public ResponseEntity<Estoque> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do estoque", required = true)
            @RequestBody Estoque entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar estoque", description = "Atualiza um registro de estoque ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Estoque atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Estoque não encontrado ou inativo")})
    public ResponseEntity<Estoque> atualizar(
            @Parameter(description = "ID do registro de estoque", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do estoque", required = true)
            @RequestBody Estoque entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir registro de estoque", description = "Desativa o registro sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Registro desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Estoque não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do registro de estoque", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
