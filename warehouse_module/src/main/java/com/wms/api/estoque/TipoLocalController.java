package com.wms.api.estoque;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.estoque.TipoLocal;
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
@RequestMapping("/api/tipos-local")
@Tag(name = "Tipos de local", description = "Cadastro de tipos de posição de armazenagem")
public class TipoLocalController extends BaseCrudController<TipoLocal> {
    public TipoLocalController(EntityManager entityManager) {
        super(entityManager, TipoLocal.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar tipos de local", description = "Retorna todos os tipos de local ativos.")
    @ApiResponse(responseCode = "200", description = "Tipos de local listados com sucesso")
    public List<TipoLocal> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar tipo de local", description = "Busca um tipo de local ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Tipo de local encontrado"),
            @ApiResponse(responseCode = "404", description = "Tipo de local não encontrado ou inativo")})
    public ResponseEntity<TipoLocal> buscarPorId(
            @Parameter(description = "ID do tipo de local", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar tipo de local", description = "Cria um tipo de local.")
    @ApiResponse(responseCode = "201", description = "Tipo de local criado com sucesso")
    public ResponseEntity<TipoLocal> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do tipo de local", required = true)
            @RequestBody TipoLocal entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar tipo de local", description = "Atualiza um tipo de local ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Tipo de local atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tipo de local não encontrado ou inativo")})
    public ResponseEntity<TipoLocal> atualizar(
            @Parameter(description = "ID do tipo de local", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados do tipo de local", required = true)
            @RequestBody TipoLocal entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir tipo de local", description = "Desativa o tipo de local sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Tipo de local desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tipo de local não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do tipo de local", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
