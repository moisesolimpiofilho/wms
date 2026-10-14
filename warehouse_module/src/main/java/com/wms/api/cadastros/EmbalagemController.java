package com.wms.api.cadastros;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.cadastros.Embalagem;
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
@RequestMapping("/api/embalagens")
@Tag(name = "Embalagens", description = "Cadastro de embalagens e suas unidades")
public class EmbalagemController extends BaseCrudController<Embalagem> {
    public EmbalagemController(EntityManager entityManager) {
        super(entityManager, Embalagem.class);
    }

    @Override
    @GetMapping
    @Operation(summary = "Listar embalagens", description = "Retorna todas as embalagens ativas.")
    @ApiResponse(responseCode = "200", description = "Embalagens listadas com sucesso")
    public List<Embalagem> listar() { return super.listar(); }

    @Override
    @GetMapping("/{id}")
    @Operation(summary = "Consultar embalagem", description = "Busca uma embalagem ativa pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Embalagem encontrada"),
            @ApiResponse(responseCode = "404", description = "Embalagem não encontrada ou inativa")})
    public ResponseEntity<Embalagem> buscarPorId(
            @Parameter(description = "ID da embalagem", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override
    @PostMapping
    @Operation(summary = "Criar embalagem", description = "Cria uma embalagem; o ID do corpo é ignorado.")
    @ApiResponse(responseCode = "201", description = "Embalagem criada com sucesso")
    public ResponseEntity<Embalagem> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados da embalagem", required = true)
            @RequestBody Embalagem entity) { return super.criar(entity); }

    @Override
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar embalagem", description = "Atualiza uma embalagem ativa.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Embalagem atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Embalagem não encontrada ou inativa")})
    public ResponseEntity<Embalagem> atualizar(
            @Parameter(description = "ID da embalagem", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados da embalagem", required = true)
            @RequestBody Embalagem entity) { return super.atualizar(id, entity); }

    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir embalagem", description = "Desativa a embalagem sem removê-la do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Embalagem desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Embalagem não encontrada ou inativa")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da embalagem", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
