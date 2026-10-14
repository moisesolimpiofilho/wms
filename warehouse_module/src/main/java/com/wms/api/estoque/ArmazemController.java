package com.wms.api.estoque;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.estoque.Armazem;
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
@RequestMapping("/api/armazens")
@Tag(name = "Armazéns", description = "Cadastro de armazéns")
public class ArmazemController extends BaseCrudController<Armazem> {
    public ArmazemController(EntityManager entityManager) {
        super(entityManager, Armazem.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar armazéns", description = "Retorna todos os armazéns ativos.")
    @ApiResponse(responseCode = "200", description = "Armazéns listados com sucesso")
    public List<Armazem> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar armazém", description = "Busca um armazém ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Armazém encontrado"),
            @ApiResponse(responseCode = "404", description = "Armazém não encontrado ou inativo")})
    public ResponseEntity<Armazem> buscarPorId(
            @Parameter(description = "ID do armazém", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar armazém", description = "Cria um armazém.")
    @ApiResponse(responseCode = "201", description = "Armazém criado com sucesso")
    public ResponseEntity<Armazem> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do armazém", required = true)
            @RequestBody Armazem entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar armazém", description = "Atualiza um armazém ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Armazém atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Armazém não encontrado ou inativo")})
    public ResponseEntity<Armazem> atualizar(
            @Parameter(description = "ID do armazém", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do armazém", required = true)
            @RequestBody Armazem entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir armazém", description = "Desativa o armazém sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Armazém desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Armazém não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do armazém", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
