package com.wms.api.cadastros;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.cadastros.Cliente;
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
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Cadastro de clientes")
public class ClienteController extends BaseCrudController<Cliente> {
    public ClienteController(EntityManager entityManager) {
        super(entityManager, Cliente.class);
    }

    @Override
    @GetMapping
    @Operation(summary = "Listar clientes", description = "Retorna todos os clientes ativos.")
    @ApiResponse(responseCode = "200", description = "Clientes listados com sucesso")
    public List<Cliente> listar() { return super.listar(); }

    @Override
    @GetMapping("/{id}")
    @Operation(summary = "Consultar cliente", description = "Busca um cliente ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado ou inativo")})
    public ResponseEntity<Cliente> buscarPorId(
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override
    @PostMapping
    @Operation(summary = "Criar cliente", description = "Cria um cliente; o ID do corpo é ignorado.")
    @ApiResponse(responseCode = "201", description = "Cliente criado com sucesso")
    public ResponseEntity<Cliente> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do cliente", required = true)
            @RequestBody Cliente entity) { return super.criar(entity); }

    @Override
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente", description = "Atualiza um cliente ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cliente atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado ou inativo")})
    public ResponseEntity<Cliente> atualizar(
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do cliente", required = true)
            @RequestBody Cliente entity) { return super.atualizar(id, entity); }

    @Override
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cliente", description = "Desativa o cliente sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Cliente desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cliente não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do cliente", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
