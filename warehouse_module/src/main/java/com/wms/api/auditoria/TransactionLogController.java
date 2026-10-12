package com.wms.api.auditoria;

import com.wms.api.base.BaseCrudController;
import com.wms.domain.auditoria.TransactionLog;
import jakarta.persistence.EntityManager;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs-transacao")
@Tag(name = "Auditoria", description = "Consulta de registros de auditoria de transações")
public class TransactionLogController extends BaseCrudController<TransactionLog> {
    public TransactionLogController(EntityManager entityManager) {
        super(entityManager, TransactionLog.class);
    }
}
