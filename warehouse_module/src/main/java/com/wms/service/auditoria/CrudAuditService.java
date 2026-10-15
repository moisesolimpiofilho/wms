package com.wms.service.auditoria;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.domain.auditoria.AuditLog;
import com.wms.domain.base.BaseEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
public class CrudAuditService {
    @PersistenceContext
    private EntityManager entityManager;

    private final ObjectMapper objectMapper;

    public CrudAuditService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String snapshot(Object entity) {
        try {
            return objectMapper.writeValueAsString(entity);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível serializar o estado para auditoria", exception);
        }
    }

    public void record(String operation, Object entity, String previousState, String currentState) {
        if (!(entity instanceof BaseEntity baseEntity) || baseEntity.getId() == null) {
            throw new IllegalArgumentException("A auditoria exige uma entidade persistida com identificador");
        }

        AuditLog auditLog = new AuditLog();
        auditLog.setEntityName(entity.getClass().getName());
        auditLog.setEntityId(baseEntity.getId());
        auditLog.setOperation(operation);
        auditLog.setUserName(resolveUserName(baseEntity));
        auditLog.setOccurredAt(LocalDateTime.now());
        auditLog.setPreviousState(previousState);
        auditLog.setCurrentState(currentState);
        entityManager.persist(auditLog);
    }

    private String resolveUserName(BaseEntity entity) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            if (attributes.getRequest().getUserPrincipal() != null) {
                return attributes.getRequest().getUserPrincipal().getName();
            }
        }
        if (hasText(entity.getUserNameLastChange())) {
            return entity.getUserNameLastChange();
        }
        if (hasText(entity.getUserNameCreator())) {
            return entity.getUserNameCreator();
        }
        return "desconhecido";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
