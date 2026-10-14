package com.wms.api.cadastros;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.cadastros.Categoria;
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
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Cadastro de categorias de produtos")
public class CategoriaController extends BaseCrudController<Categoria> {
    public CategoriaController(EntityManager entityManager) {
        super(entityManager, Categoria.class);
    }

    @Override
    @GetMapping
    @Operation(summary = "Listar categorias", description = "Retorna todas as categorias ativas.")
    @ApiResponse(responseCode = "200", description = "Categorias listadas com sucesso")
    public List<Categoria> listar() {
        return super.listar();
    }

    @Override
    @GetMapping("/{id}")
    @Operation(summary = "Consultar categoria", description = "Busca uma categoria ativa pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria encontrada"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou inativa")
    })
    public ResponseEntity<Categoria> buscarPorId(
            @Parameter(description = "ID da categoria", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override
    @PostMapping
    @Operation(summary = "Criar categoria", description = "Cria uma categoria. O ID informado no corpo é ignorado.")
    @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso")
    @ApiResponse(responseCode = "404", description = "Dados da categoria inválidos")
    public ResponseEntity<Categoria> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados da categoria", required = true)
            @RequestBody Categoria entity) {

        if (entity == null
                || entity.getCode() == null
                || entity.getCode().isBlank()
                || entity.getDescription() == null
                || entity.getDescription().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        return super.criar(entity);
    }

    @Override
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria", description = "Atualiza uma categoria ativa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Categoria atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados da categoria inválidos"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou inativa")
    })
    public ResponseEntity<Categoria> atualizar(
            @Parameter(description = "ID da categoria", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados da categoria", required = true)
            @RequestBody Categoria entity) {

        if (entity == null
                || entity.getCode() == null
                || entity.getCode().isBlank()
                || entity.getDescription() == null
                || entity.getDescription().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        
        return super.atualizar(id, entity);
    }

    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir categoria", description = "Desativa a categoria sem removê-la do banco.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Categoria desativada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada ou inativa")
    })
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID da categoria", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
