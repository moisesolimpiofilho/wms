package com.wms.api.cadastros;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.cadastros.Produto;
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
@RequestMapping("/api/produtos")
@Tag(name = "Produtos", description = "Cadastro de produtos")
public class ProdutoController extends BaseCrudController<Produto> {
    public ProdutoController(EntityManager entityManager) {
        super(entityManager, Produto.class);
    }

    @Override
    @GetMapping
    @Operation(summary = "Listar produtos", description = "Retorna todos os produtos ativos.")
    @ApiResponse(responseCode = "200", description = "Produtos listados com sucesso")
    public List<Produto> listar() { return super.listar(); }

    @Override
    @GetMapping("/{id}")
    @Operation(summary = "Consultar produto", description = "Busca um produto ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Produto encontrado"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado ou inativo")})
    public ResponseEntity<Produto> buscarPorId(
            @Parameter(description = "ID do produto", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override
    @PostMapping
    @Operation(summary = "Criar produto", description = "Cria um produto; o ID do corpo é ignorado.")
    @ApiResponse(responseCode = "201", description = "Produto criado com sucesso")
    public ResponseEntity<Produto> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do produto", required = true)
            @RequestBody Produto entity) { return super.criar(entity); }

    @Override
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar produto", description = "Atualiza um produto ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Produto atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado ou inativo")})
    public ResponseEntity<Produto> atualizar(
            @Parameter(description = "ID do produto", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do produto", required = true)
            @RequestBody Produto entity) { return super.atualizar(id, entity); }

    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir produto", description = "Desativa o produto sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Produto desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do produto", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
