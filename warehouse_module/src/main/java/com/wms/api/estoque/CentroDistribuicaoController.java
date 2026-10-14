package com.wms.api.estoque;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.estoque.CentroDistribuicao;
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
@RequestMapping("/api/centros-distribuicao")
@Tag(name = "Centros de distribuição", description = "Cadastro de centros de distribuição")
public class CentroDistribuicaoController extends BaseCrudController<CentroDistribuicao> {
    public CentroDistribuicaoController(EntityManager entityManager) {
        super(entityManager, CentroDistribuicao.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar centros de distribuição", description = "Retorna todos os centros ativos.")
    @ApiResponse(responseCode = "200", description = "Centros listados com sucesso")
    public List<CentroDistribuicao> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar centro de distribuição", description = "Busca um centro ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Centro encontrado"),
            @ApiResponse(responseCode = "404", description = "Centro não encontrado ou inativo")})
    public ResponseEntity<CentroDistribuicao> buscarPorId(
            @Parameter(description = "ID do centro", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar centro de distribuição", description = "Cria um centro de distribuição.")
    @ApiResponse(responseCode = "201", description = "Centro criado com sucesso")
    public ResponseEntity<CentroDistribuicao> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do centro", required = true)
            @RequestBody CentroDistribuicao entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar centro de distribuição", description = "Atualiza um centro ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Centro atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Centro não encontrado ou inativo")})
    public ResponseEntity<CentroDistribuicao> atualizar(
            @Parameter(description = "ID do centro", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do centro", required = true)
            @RequestBody CentroDistribuicao entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir centro de distribuição", description = "Desativa o centro sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Centro desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Centro não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do centro", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
