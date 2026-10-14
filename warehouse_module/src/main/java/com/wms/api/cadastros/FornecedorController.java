package com.wms.api.cadastros;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.cadastros.Fornecedor;
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
@RequestMapping("/api/fornecedores")
@Tag(name = "Fornecedores", description = "Cadastro de fornecedores")
public class FornecedorController extends BaseCrudController<Fornecedor> {
    public FornecedorController(EntityManager entityManager) {
        super(entityManager, Fornecedor.class);
    }

    @Override
    @GetMapping
    @Operation(summary = "Listar fornecedores", description = "Retorna todos os fornecedores ativos.")
    @ApiResponse(responseCode = "200", description = "Fornecedores listados com sucesso")
    public List<Fornecedor> listar() { return super.listar(); }

    @Override
    @GetMapping("/{id}")
    @Operation(summary = "Consultar fornecedor", description = "Busca um fornecedor ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Fornecedor encontrado"),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado ou inativo")})
    public ResponseEntity<Fornecedor> buscarPorId(
            @Parameter(description = "ID do fornecedor", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override
    @PostMapping
    @Operation(summary = "Criar fornecedor", description = "Cria um fornecedor; o ID do corpo é ignorado.")
    @ApiResponse(responseCode = "201", description = "Fornecedor criado com sucesso")
    public ResponseEntity<Fornecedor> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do fornecedor", required = true)
            @RequestBody Fornecedor entity) { return super.criar(entity); }

    @Override
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar fornecedor", description = "Atualiza um fornecedor ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Fornecedor atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado ou inativo")})
    public ResponseEntity<Fornecedor> atualizar(
            @Parameter(description = "ID do fornecedor", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do fornecedor", required = true)
            @RequestBody Fornecedor entity) { return super.atualizar(id, entity); }

    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir fornecedor", description = "Desativa o fornecedor sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Fornecedor desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do fornecedor", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
