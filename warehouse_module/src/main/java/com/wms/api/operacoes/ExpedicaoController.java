package com.wms.api.operacoes;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.operacoes.Expedicao;
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
@RequestMapping("/api/expedicoes")
@Tag(name = "Expedições", description = "Gestão das expedições de pedidos")
public class ExpedicaoController extends BaseCrudController<Expedicao> {
    public ExpedicaoController(EntityManager entityManager) {
        super(entityManager, Expedicao.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar expedições", description = "Retorna todas as expedições ativas.")
    @ApiResponse(responseCode = "200", description = "Expedições listadas com sucesso")
    public List<Expedicao> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar expedição", description = "Busca uma expedição ativa pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Expedição encontrada"),
            @ApiResponse(responseCode = "404", description = "Expedição não encontrada ou inativa")})
    public ResponseEntity<Expedicao> buscarPorId(
            @Parameter(description = "ID da expedição", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar expedição", description = "Registra uma expedição.")
    @ApiResponse(responseCode = "201", description = "Expedição criada com sucesso")
    public ResponseEntity<Expedicao> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados da expedição", required = true)
            @RequestBody Expedicao entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar expedição", description = "Atualiza uma expedição ativa.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Expedição atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Expedição não encontrada ou inativa")})
    public ResponseEntity<Expedicao> atualizar(
            @Parameter(description = "ID da expedição", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados da expedição", required = true)
            @RequestBody Expedicao entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir expedição", description = "Desativa a expedição sem removê-la do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Expedição desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Expedição não encontrada ou inativa")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da expedição", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
