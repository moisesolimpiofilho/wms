package com.wms.api.base;

import com.wms.domain.base.BaseEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.metamodel.EntityType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "CRUD", description = "Operações padrão de consulta e manutenção de entidades")
public abstract class BaseCrudController<T extends BaseEntity> {
    private final EntityManager entityManager;
    private final Class<T> entityType;

    protected BaseCrudController(EntityManager entityManager, Class<T> entityType) {
        this.entityManager = entityManager;
        this.entityType = entityType;
    }

    @GetMapping
    @Operation(summary = "Listar registros", description = "Retorna os registros ativos da entidade.")
    @ApiResponse(responseCode = "200", description = "Lista de registros retornada com sucesso")
    @Transactional(readOnly = true)
    public List<T> listar() {
        EntityType<T> jpaEntity = entityManager.getMetamodel().entity(entityType);
        String query = "select entity from " + jpaEntity.getName() + " entity where entity.active = true";
        return entityManager.createQuery(query, entityType).getResultList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar registro por ID", description = "Retorna um registro ativo pelo identificador.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Transactional(readOnly = true)
    public ResponseEntity<T> buscarPorId(
            @Parameter(description = "Identificador do registro", example = "1")
            @PathVariable Long id) {
        T entity = buscarAtivoPorId(id);
        return entity == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(entity);
    }

    @PostMapping
    @Operation(summary = "Criar registro", description = "Cria um registro da entidade. O ID enviado no corpo é ignorado.")
    @ApiResponse(responseCode = "201", description = "Registro criado com sucesso")
    @Transactional
    public ResponseEntity<T> criar(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados do registro a criar",
                    required = true)
            @RequestBody T entity) {
        entity.setId(null);
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
    @Operation(summary = "Atualizar registro", description = "Atualiza os dados de um registro ativo identificado pelo ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro atualizado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Transactional
    public ResponseEntity<T> atualizar(
            @Parameter(description = "Identificador do registro", example = "1")
            @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Dados atualizados do registro",
                    required = true)
            @RequestBody T entity) {
        T currentEntity = buscarAtivoPorId(id);
        if (currentEntity == null) {
            return ResponseEntity.notFound().build();
        }

        entity.setId(id);
        if (entity.getActive() == null) {
            entity.setActive(currentEntity.getActive());
        }
        return ResponseEntity.ok(entityManager.merge(entity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir registro", description = "Desativa logicamente o registro, mantendo seus dados no banco.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Registro excluído com sucesso"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado")
    })
    @Transactional
    public ResponseEntity<Void> excluir(
            @Parameter(description = "Identificador do registro", example = "1")
            @PathVariable Long id) {
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
