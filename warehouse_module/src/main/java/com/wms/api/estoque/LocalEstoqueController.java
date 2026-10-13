package com.wms.api.estoque;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.estoque.LocalEstoque;
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
@RequestMapping("/api/locais-estoque")
@Tag(name = "Locais de estoque", description = "Cadastro de posições de armazenagem")
public class LocalEstoqueController extends BaseCrudController<LocalEstoque> {
    public LocalEstoqueController(EntityManager entityManager) {
        super(entityManager, LocalEstoque.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar locais de estoque", description = "Retorna todos os locais ativos.")
    @ApiResponse(responseCode = "200", description = "Locais listados com sucesso")
    public List<LocalEstoque> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar local de estoque", description = "Busca um local ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Local encontrado"),
            @ApiResponse(responseCode = "404", description = "Local não encontrado ou inativo")})
    public ResponseEntity<LocalEstoque> buscarPorId(
            @Parameter(description = "ID do local", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar local de estoque", description = "Cria uma posição de armazenagem.")
    @ApiResponse(responseCode = "201", description = "Local criado com sucesso")
    public ResponseEntity<LocalEstoque> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do local", required = true)
            @RequestBody LocalEstoque entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar local de estoque", description = "Atualiza um local ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Local atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Local não encontrado ou inativo")})
    public ResponseEntity<LocalEstoque> atualizar(
            @Parameter(description = "ID do local", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do local", required = true)
            @RequestBody LocalEstoque entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir local de estoque", description = "Desativa o local sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Local desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Local não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do local", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
