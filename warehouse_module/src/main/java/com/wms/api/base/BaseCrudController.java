package com.wms.api.base;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
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

    protected BaseCrudController(EntityManager entityManager, Class<T> entityType) {
        this.entityManager = entityManager;
        this.entityType = entityType;
        this.requestValidator = new EntityRequestValidator(entityManager);
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
        entity.setId(null);
        requestValidator.validate(entity, entityType);
        if (entity.getActive() == null) {
            entity.setActive(true);
        }
        entityManager.persist(entity);
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

        entity.setId(id);
        requestValidator.validate(entity, entityType);
        if (entity.getActive() == null) {
            entity.setActive(currentEntity.getActive());
        }
        return ResponseEntity.ok(entityManager.merge(entity));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        T entity = buscarAtivoPorId(id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }

        entity.setActive(false);
        entity.setLastChangeDate(java.time.LocalDateTime.now());
        return ResponseEntity.noContent().build();
    }

    private T buscarAtivoPorId(Long id) {
        T entity = entityManager.find(entityType, id);
        return entity != null && Boolean.TRUE.equals(entity.getActive()) ? entity : null;
    }
}
