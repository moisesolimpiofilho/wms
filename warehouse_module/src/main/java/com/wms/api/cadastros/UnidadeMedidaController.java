package com.wms.api.cadastros;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.cadastros.UnidadeMedida;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/unidades-medida")
@Tag(name = "Unidades de medida", description = "Cadastro de unidades de medida")
public class UnidadeMedidaController extends BaseCrudController<UnidadeMedida> {
    public UnidadeMedidaController(EntityManager entityManager) {
        super(entityManager, UnidadeMedida.class);
    }

    @Override
    @GetMapping
    @Operation(summary = "Listar unidades de medida", description = "Retorna todas as unidades de medida ativas.")
    @ApiResponse(responseCode = "200", description = "Unidades de medida listadas com sucesso")
    public List<UnidadeMedida> listar() { return super.listar(); }

    @Override
    @GetMapping("/{id}")
    @Operation(summary = "Consultar unidade de medida", description = "Busca uma unidade de medida ativa pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Unidade de medida encontrada"),
            @ApiResponse(responseCode = "404", description = "Unidade de medida não encontrada ou inativa")})
    public ResponseEntity<UnidadeMedida> buscarPorId(
            @Parameter(description = "ID da unidade de medida", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override
    @PostMapping
    @Operation(summary = "Criar unidade de medida", description = "Cria uma unidade de medida; o ID do corpo é ignorado.")
    @ApiResponse(responseCode = "201", description = "Unidade de medida criada com sucesso")
    public ResponseEntity<UnidadeMedida> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados da unidade de medida", required = true)
            @RequestBody UnidadeMedida entity) { return super.criar(entity); }

    @Override
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar unidade de medida", description = "Atualiza uma unidade de medida ativa.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Unidade de medida atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Unidade de medida não encontrada ou inativa")})
    public ResponseEntity<UnidadeMedida> atualizar(
            @Parameter(description = "ID da unidade de medida", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados da unidade de medida", required = true)
            @RequestBody UnidadeMedida entity) { return super.atualizar(id, entity); }

    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir unidade de medida", description = "Desativa a unidade de medida sem removê-la do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Unidade de medida desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Unidade de medida não encontrada ou inativa")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da unidade de medida", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
