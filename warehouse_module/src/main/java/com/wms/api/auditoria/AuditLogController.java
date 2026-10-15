package com.wms.api.auditoria;

import com.wms.domain.auditoria.AuditLog;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/logs-transacao")
@Tag(name = "Auditoria", description = "Consulta somente leitura dos registros automáticos de auditoria")
public class AuditLogController {
    private final EntityManager entityManager;

    public AuditLogController(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @GetMapping
    @Transactional(readOnly = true)
    @Operation(summary = "Listar registros de auditoria",
            description = "Retorna os registros imutáveis gerados automaticamente pelos CRUDs.")
    @ApiResponse(responseCode = "200", description = "Registros listados com sucesso")
    public List<AuditLog> listar() {
        return entityManager.createQuery(
                        "select log from AuditLog log order by log.occurredAt desc, log.id desc",
                        AuditLog.class)
                .getResultList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "Consultar registro de auditoria", description = "Busca um registro pelo ID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")})
    public ResponseEntity<AuditLog> buscarPorId(
            @Parameter(description = "ID do registro", example = "1") @PathVariable Long id) {
        AuditLog auditLog = entityManager.find(AuditLog.class, id);
        return auditLog == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(auditLog);
    }
}
