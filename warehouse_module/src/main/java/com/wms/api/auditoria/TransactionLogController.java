package com.wms.api.auditoria;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.auditoria.TransactionLog;
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
@RequestMapping("/api/logs-transacao")
@Tag(name = "Auditoria", description = "Consulta de registros de auditoria de transações")
public class TransactionLogController extends BaseCrudController<TransactionLog> {
    public TransactionLogController(EntityManager entityManager) {
        super(entityManager, TransactionLog.class);
    }

    @Override @GetMapping
    @Operation(summary = "Listar registros de auditoria", description = "Retorna todos os registros de auditoria ativos.")
    @ApiResponse(responseCode = "200", description = "Registros listados com sucesso")
    public List<TransactionLog> listar() { return super.listar(); }

    @Override @GetMapping("/{id}")
    @Operation(summary = "Consultar registro de auditoria", description = "Busca um registro ativo pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado ou inativo")})
    public ResponseEntity<TransactionLog> buscarPorId(
            @Parameter(description = "ID do registro", example = "1") @PathVariable Long id) {
        return super.buscarPorId(id);
    }

    @Override @PostMapping
    @Operation(summary = "Criar registro de auditoria", description = "Registra uma entrada de auditoria.")
    @ApiResponse(responseCode = "201", description = "Registro criado com sucesso")
    public ResponseEntity<TransactionLog> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados do registro", required = true)
            @RequestBody TransactionLog entity) { return super.criar(entity); }

    @Override @PutMapping("/{id}")
    @Operation(summary = "Atualizar registro de auditoria", description = "Atualiza um registro ativo.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Registro atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado ou inativo")})
    public ResponseEntity<TransactionLog> atualizar(
            @Parameter(description = "ID do registro", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Dados atualizados do registro", required = true)
            @RequestBody TransactionLog entity) { return super.atualizar(id, entity); }

    @Override @DeleteMapping("/{id}")
    @Operation(summary = "Excluir registro de auditoria", description = "Desativa o registro sem removê-lo do banco.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Registro desativado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado ou inativo")})
    public ResponseEntity<Void> excluir(
            @Parameter(description = "ID do registro", example = "1") @PathVariable Long id) {
        return super.excluir(id);
    }
}
