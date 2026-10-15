package com.wms.api.base;

import com.wms.domain.base.BaseEntity;
import com.wms.service.auditoria.CrudAuditService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

public abstract class BaseCrudController<T extends BaseEntity> {
    private final EntityManager entityManager;
    private final Class<T> entityType;
    private final EntityRequestValidator requestValidator;
    private CrudAuditService auditService;

    protected BaseCrudController(EntityManager entityManager, Class<T> entityType) {
        this.entityManager = entityManager;
        this.entityType = entityType;
        this.requestValidator = new EntityRequestValidator(entityManager);
    }

    @Autowired
    protected void setAuditService(CrudAuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @Transactional(readOnly = true)
    public List<T> listar() {
        EntityType<T> jpaEntity = entityManager.getMetamodel().entity(entityType);
        String query = "select entity from " + jpaEntity.getName() + " entity where entity.active = true";
        return entityManager.createQuery(query, entityType).getResultList();
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<T> buscarPorId(@PathVariable Long id) {
        T entity = buscarAtivoPorId(id);
        return entity == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(entity);
    }

    @PostMapping
    @Transactional
    public ResponseEntity<T> criar(@RequestBody T entity) {
        if (entity == null) {
            return ResponseEntity.badRequest().build();
        }
        entity.setId(null);
        requestValidator.validate(entity, entityType);
        if (entity.getActive() == null) {
            entity.setActive(true);
        }
        entityManager.persist(entity);
        entityManager.flush();
        auditService.record("CREATE", entity, null, auditService.snapshot(entity));
        return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(entity.getId())
                        .toUri())
                .body(entity);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<T> atualizar(@PathVariable Long id, @RequestBody T entity) {
        T currentEntity = buscarAtivoPorId(id);
        if (currentEntity == null) {
            return ResponseEntity.notFound().build();
        }

        if (entity == null) {
            return ResponseEntity.badRequest().build();
        }
        String previousState = auditService.snapshot(currentEntity);
        entity.setId(id);
        requestValidator.validate(entity, entityType);
        if (entity.getActive() == null) {
            entity.setActive(currentEntity.getActive());
        }
        T updatedEntity = entityManager.merge(entity);
        entityManager.flush();
        auditService.record("UPDATE", updatedEntity, previousState, auditService.snapshot(updatedEntity));
        return ResponseEntity.ok(updatedEntity);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        T entity = buscarAtivoPorId(id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }

        String previousState = auditService.snapshot(entity);
        entity.setActive(false);
        entity.setLastChangeDate(java.time.LocalDateTime.now());
        entityManager.flush();
        auditService.record("DELETE", entity, previousState, auditService.snapshot(entity));
        return ResponseEntity.noContent().build();
    }

    private T buscarAtivoPorId(Long id) {
        T entity = entityManager.find(entityType, id);
        return entity != null && Boolean.TRUE.equals(entity.getActive()) ? entity : null;
    }
}
