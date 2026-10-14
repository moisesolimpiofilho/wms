package com.wms.api.identificacao;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.identificacao.UnidadeLogistica;
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
@RequestMapping("/api/unidades-logisticas")
@Tag(name = "Unidades logísticas", description = "Cadastro e consulta de unidades logísticas")
public class UnidadeLogisticaController extends BaseCrudController<UnidadeLogistica> {
    public UnidadeLogisticaController(EntityManager entityManager) {
        super(entityManager, UnidadeLogistica.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar unidades logísticas", description = "Retorna todas as unidades logísticas ativas.")
    @ApiResponse(responseCode = "200", description = "Unidades logísticas listadas com sucesso")
    public List<UnidadeLogistica> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar unidade logística", description = "Busca uma unidade logística ativa pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Unidade logística encontrada"),
            @ApiResponse(responseCode = "404", description = "Unidade logística não encontrada ou inativa")})
    public ResponseEntity<UnidadeLogistica> buscarPorId(
            @Parameter(description = "ID da unidade logística", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar unidade logística", description = "Cadastra uma unidade logística.")
    @ApiResponse(responseCode = "201", description = "Unidade logística criada com sucesso")
    public ResponseEntity<UnidadeLogistica> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da unidade logística", required = true)
            @RequestBody UnidadeLogistica entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar unidade logística", description = "Atualiza uma unidade logística ativa.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Unidade logística atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Unidade logística não encontrada ou inativa")})
    public ResponseEntity<UnidadeLogistica> atualizar(
            @Parameter(description = "ID da unidade logística", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados da unidade logística", required = true)
            @RequestBody UnidadeLogistica entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir unidade logística", description = "Desativa a unidade logística sem removê-la do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Unidade logística desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Unidade logística não encontrada ou inativa")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da unidade logística", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
