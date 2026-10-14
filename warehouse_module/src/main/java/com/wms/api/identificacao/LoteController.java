package com.wms.api.identificacao;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.identificacao.Lote;
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
@RequestMapping("/api/lotes")
@Tag(name = "Lotes", description = "Cadastro e consulta de lotes de produtos")
public class LoteController extends BaseCrudController<Lote> {
    public LoteController(EntityManager entityManager) {
        super(entityManager, Lote.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar lotes", description = "Retorna todos os lotes ativos.")
    @ApiResponse(responseCode = "200", description = "Lotes listados com sucesso")
    public List<Lote> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar lote", description = "Busca um lote ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lote encontrado"),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado ou inativo")})
    public ResponseEntity<Lote> buscarPorId(
            @Parameter(description = "ID do lote", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar lote", description = "Cadastra um lote de produto.")
    @ApiResponse(responseCode = "201", description = "Lote criado com sucesso")
    public ResponseEntity<Lote> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do lote", required = true)
            @RequestBody Lote entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar lote", description = "Atualiza um lote ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lote atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado ou inativo")})
    public ResponseEntity<Lote> atualizar(
            @Parameter(description = "ID do lote", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do lote", required = true)
            @RequestBody Lote entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir lote", description = "Desativa o lote sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Lote desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Lote não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do lote", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
