package com.wms.api.estoque;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.estoque.ZonaArmazenagem;
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
@RequestMapping("/api/zonas-armazenagem")
@Tag(name = "Zonas de armazenagem", description = "Cadastro de zonas nos armazéns")
public class ZonaArmazenagemController extends BaseCrudController<ZonaArmazenagem> {
    public ZonaArmazenagemController(EntityManager entityManager) {
        super(entityManager, ZonaArmazenagem.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar zonas de armazenagem", description = "Retorna todas as zonas cadastradas.")
    @ApiResponse(responseCode = "200", description = "Zonas listadas com sucesso")
    public List<ZonaArmazenagem> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar zona de armazenagem", description = "Busca uma zona ativa pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Zona encontrada"),
            @ApiResponse(responseCode = "404", description = "Zona não encontrada ou inativa")})
    public ResponseEntity<ZonaArmazenagem> buscarPorId(
            @Parameter(description = "ID da zona", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar zona de armazenagem", description = "Cria uma zona de armazenagem.")
    @ApiResponse(responseCode = "201", description = "Zona criada com sucesso")
    public ResponseEntity<ZonaArmazenagem> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados da zona", required = true)
            @RequestBody ZonaArmazenagem entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar zona de armazenagem", description = "Atualiza uma zona ativa.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Zona atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Zona não encontrada ou inativa")})
    public ResponseEntity<ZonaArmazenagem> atualizar(
            @Parameter(description = "ID da zona", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados da zona", required = true)
            @RequestBody ZonaArmazenagem entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir zona de armazenagem", description = "Desativa a zona sem removê-la do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Zona desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Zona não encontrada ou inativa")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da zona", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
