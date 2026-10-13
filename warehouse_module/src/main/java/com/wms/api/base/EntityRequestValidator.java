package com.wms.api.base;

import com.wms.domain.cadastros.Categoria;
import com.wms.domain.cadastros.Cliente;
import com.wms.domain.cadastros.Embalagem;
import com.wms.domain.cadastros.Fornecedor;
import com.wms.domain.cadastros.Produto;
import com.wms.domain.cadastros.Segmento;
import com.wms.domain.cadastros.UnidadeMedida;
import com.wms.domain.estoque.Armazem;
import com.wms.domain.estoque.CentroDistribuicao;
import com.wms.domain.estoque.LocalEstoque;
import com.wms.domain.estoque.TipoLocal;
import com.wms.domain.estoque.ZonaArmazenagem;
import com.wms.domain.identificacao.Lote;
import com.wms.domain.identificacao.MovimentacaoEstoque;
import com.wms.domain.identificacao.Selo;
import com.wms.domain.identificacao.UnidadeLogistica;
import com.wms.domain.operacoes.Inventario;
import com.wms.domain.operacoes.OrdemEstoque;
import com.wms.domain.operacoes.Pedido;
import com.wms.domain.operacoes.Recebimento;
import com.wms.domain.operacoes.Separacao;
import com.wms.domain.operacoes.ajustes.MotivoMovimentacao;
import com.wms.domain.operacoes.complementos.OrdemEstoqueItem;
import com.wms.domain.operacoes.itens.PedidoItem;
import jakarta.persistence.EntityManager;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.Map;

public class EntityRequestValidator {
    private static final Map<String, Class<?>> REFERENCE_TYPES = Map.ofEntries(
            Map.entry("categoryId", Categoria.class),
            Map.entry("segmentId", Segmento.class),
            Map.entry("unitMeasureId", UnidadeMedida.class),
            Map.entry("centerDistributionId", CentroDistribuicao.class),
            Map.entry("zoneId", ZonaArmazenagem.class),
            Map.entry("locationTypeId", TipoLocal.class),
            Map.entry("warehouseId", Armazem.class),
            Map.entry("originWarehouseId", Armazem.class),
            Map.entry("destinationWarehouseId", Armazem.class),
            Map.entry("productId", Produto.class),
            Map.entry("batchId", Lote.class),
            Map.entry("locationId", LocalEstoque.class),
            Map.entry("originLocationId", LocalEstoque.class),
            Map.entry("destinationLocationId", LocalEstoque.class),
            Map.entry("logisticUnitId", UnidadeLogistica.class),
            Map.entry("packagingId", Embalagem.class),
            Map.entry("customerId", Cliente.class),
            Map.entry("supplierId", Fornecedor.class),
            Map.entry("reasonId", MotivoMovimentacao.class),
            Map.entry("inventoryId", Inventario.class),
            Map.entry("receiptId", Recebimento.class),
            Map.entry("separationId", Separacao.class),
            Map.entry("orderItemId", PedidoItem.class),
            Map.entry("sealId", Selo.class),
            Map.entry("movementId", MovimentacaoEstoque.class)
    );

    private final EntityManager entityManager;

    public EntityRequestValidator(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void validate(Object entity, Class<?> entityType) {
        validateUniqueField(entity, entityType, "code", false);
        validateUniqueField(entity, entityType, "document", true);
        validateReferences(entity, entityType);
    }

    public static Class<?> referenceType(Class<?> ownerType, String fieldName) {
        if ("orderId".equals(fieldName)) {
            return ownerType == OrdemEstoqueItem.class ? OrdemEstoque.class : Pedido.class;
        }
        if ("parentUnitId".equals(fieldName)) {
            return UnidadeLogistica.class;
        }
        return REFERENCE_TYPES.get(fieldName);
    }

    private void validateUniqueField(Object entity, Class<?> entityType, String fieldName, boolean document) {
        Field field = findField(entityType, fieldName);
        if (field == null) {
            return;
        }

        Object value = getValue(field, entity);
        if (value == null) {
            return;
        }

        String text = value.toString();
        if (document) {
            if (!com.wms.domain.validation.BrazilianDocument.isValid(text)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        fieldName + " deve ser um CPF ou CNPJ válido.");
            }
            text = com.wms.domain.validation.BrazilianDocument.normalize(text);
        }

        String jpql = "select count(entity) from " + entityType.getSimpleName()
                + " entity where entity." + fieldName + " = :value";
        Long id = (Long) getValue(findField(entityType, "id"), entity);
        if (id != null) {
            jpql += " and entity.id <> :id";
        }

        var query = entityManager.createQuery(jpql, Long.class).setParameter("value", text);
        if (id != null) {
            query.setParameter("id", id);
        }
        if (query.getSingleResult() > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    fieldName + " já está cadastrado.");
        }
    }

    private void validateReferences(Object entity, Class<?> entityType) {
        for (Class<?> current = entityType; current != null && current != Object.class;
             current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                Class<?> targetType = field.getType() == Long.class
                        ? referenceType(entityType, field.getName())
                        : null;
                if (targetType == null) {
                    continue;
                }

                Long id = (Long) getValue(field, entity);
                if (id != null && entityManager.find(targetType, id) == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            field.getName() + " referencia um registro inexistente.");
                }
            }
        }
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null && current != Object.class;
             current = current.getSuperclass()) {
            try {
                Field field = current.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                // Continue through the entity's superclass hierarchy.
            }
        }
        return null;
    }

    private static Object getValue(Field field, Object entity) {
        if (field == null) {
            return null;
        }
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException exception) {
            throw new IllegalStateException("Não foi possível validar " + field.getName() + ".", exception);
        }
    }
}
