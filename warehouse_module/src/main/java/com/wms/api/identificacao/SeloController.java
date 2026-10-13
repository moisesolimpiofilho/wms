package com.wms.api.identificacao;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.identificacao.Selo;
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
@RequestMapping("/api/selos")
@Tag(name = "Selos", description = "Cadastro e consulta de etiquetas de estoque")
public class SeloController extends BaseCrudController<Selo> {
    public SeloController(EntityManager entityManager) {
        super(entityManager, Selo.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar selos", description = "Retorna todos os selos ativos.")
    @ApiResponse(responseCode = "200", description = "Selos listados com sucesso")
    public List<Selo> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar selo", description = "Busca um selo ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Selo encontrado"),
            @ApiResponse(responseCode = "404", description = "Selo não encontrado ou inativo")})
    public ResponseEntity<Selo> buscarPorId(
            @Parameter(description = "ID do selo", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar selo", description = "Cadastra um selo de identificação.")
    @ApiResponse(responseCode = "201", description = "Selo criado com sucesso")
    public ResponseEntity<Selo> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do selo", required = true)
            @RequestBody Selo entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar selo", description = "Atualiza um selo ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Selo atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Selo não encontrado ou inativo")})
    public ResponseEntity<Selo> atualizar(
            @Parameter(description = "ID do selo", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do selo", required = true)
            @RequestBody Selo entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir selo", description = "Desativa o selo sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Selo desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Selo não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do selo", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
