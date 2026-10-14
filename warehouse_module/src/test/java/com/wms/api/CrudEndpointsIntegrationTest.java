package com.wms.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.Main;
import com.wms.domain.auditoria.TransactionLog;
import com.wms.domain.base.BaseEntity;
import com.wms.api.base.EntityRequestValidator;
import com.wms.domain.cadastros.Categoria;
import com.wms.domain.cadastros.Cliente;
import com.wms.domain.cadastros.Embalagem;
import com.wms.domain.cadastros.Fornecedor;
import com.wms.domain.cadastros.Produto;
import com.wms.domain.cadastros.Segmento;
import com.wms.domain.cadastros.UnidadeMedida;
import com.wms.domain.estoque.Armazem;
import com.wms.domain.estoque.CentroDistribuicao;
import com.wms.domain.estoque.Estoque;
import com.wms.domain.estoque.LocalEstoque;
import com.wms.domain.estoque.TipoLocal;
import com.wms.domain.estoque.ZonaArmazenagem;
import com.wms.domain.identificacao.Lote;
import com.wms.domain.identificacao.MovimentacaoEstoque;
import com.wms.domain.identificacao.Selo;
import com.wms.domain.identificacao.UnidadeLogistica;
import com.wms.domain.operacoes.Expedicao;
import com.wms.domain.operacoes.Inventario;
import com.wms.domain.operacoes.OrdemEstoque;
import com.wms.domain.operacoes.OrdemMovimentacao;
import com.wms.domain.operacoes.Pedido;
import com.wms.domain.operacoes.Recebimento;
import com.wms.domain.operacoes.Separacao;
import com.wms.domain.operacoes.ajustes.MotivoMovimentacao;
import com.wms.domain.operacoes.complementos.OrdemEstoqueItem;
import com.wms.domain.operacoes.itens.InventarioItem;
import com.wms.domain.operacoes.itens.PedidoItem;
import com.wms.domain.operacoes.itens.RecebimentoItem;
import com.wms.domain.operacoes.itens.SeparacaoItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Column;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.aop.support.AopUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        classes = Main.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:wms-crud-tests;MODE=MySQL;DB_CLOSE_DELAY=-1",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.jpa.hibernate.ddl-auto=create-drop"
        })
class CrudEndpointsIntegrationTest {
    @Autowired
    private WebApplicationContext applicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ApplicationContext springContext;

    private MockMvc mockMvc;
    private final Map<Class<?>, Long> seededReferenceIds = new HashMap<>();

    @BeforeEach
    void setUpMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext).build();
        seededReferenceIds.clear();
    }

    @Test
    @Transactional
    void rejectsDuplicateCodeForTheSameEntity() throws Exception {
        Map<String, Object> payload = validPayload(Cliente.class);
        payload.put("code", "CLIENTE-DUPLICADO");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    void rejectsUpdatingAnEntityWithAnotherEntitiesCode() throws Exception {
        Map<String, Object> firstClient = validPayload(Cliente.class);
        firstClient.put("code", "CLIENTE-ORIGINAL");
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstClient)))
                .andExpect(status().isCreated());

        Map<String, Object> secondClient = validPayload(Cliente.class);
        secondClient.put("code", "CLIENTE-SECUNDARIO");
        String created = mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondClient)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long secondClientId = objectMapper.readTree(created).get("id").asLong();

        secondClient.put("code", "CLIENTE-ORIGINAL");
        mockMvc.perform(put("/api/clientes/" + secondClientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondClient)))
                .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    void rejectsInvalidCpfAndCnpj() throws Exception {
        for (String document : new String[]{"123.456.789-00", "11.222.333/0001-82"}) {
            Map<String, Object> payload = validPayload(Cliente.class);
            payload.put("document", document);
            mockMvc.perform(post("/api/clientes")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @Transactional
    void acceptsAndNormalizesValidCpfAndCnpj() throws Exception {
        Map<String, Object> client = validPayload(Cliente.class);
        client.put("document", "529.982.247-25");
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(client)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.document").value("52998224725"));

        Map<String, Object> supplier = validPayload(Fornecedor.class);
        supplier.put("document", "11.222.333/0001-81");
        mockMvc.perform(post("/api/fornecedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplier)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.document").value("11222333000181"));
    }

    @Test
    @Transactional
    void rejectsDuplicateCpfAfterNormalizingMask() throws Exception {
        Map<String, Object> client = validPayload(Cliente.class);
        client.put("document", "529.982.247-25");
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(client)))
                .andExpect(status().isCreated());

        client.put("code", "OUTRO-CLIENTE");
        client.put("document", "52998224725");
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(client)))
                .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    void rejectsDuplicateCnpjForSuppliers() throws Exception {
        Map<String, Object> supplier = validPayload(Fornecedor.class);
        supplier.put("document", "11.222.333/0001-81");
        mockMvc.perform(post("/api/fornecedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplier)))
                .andExpect(status().isCreated());

        supplier.put("code", "OUTRO-FORNECEDOR");
        supplier.put("document", "11222333000181");
        mockMvc.perform(post("/api/fornecedores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(supplier)))
                .andExpect(status().isConflict());
    }

    @Test
    @Transactional
    void rejectsReferencesToMissingRecordsEvenForOptionalFields() throws Exception {
        Map<String, Object> product = validPayload(Produto.class);
        product.put("segmentId", Long.MAX_VALUE);

        mockMvc.perform(post("/api/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest(name = "{0} supports the complete CRUD lifecycle")
    @MethodSource("entityEndpoints")
    @Transactional
    void completesCrudLifecycleForEveryEntity(Endpoint endpoint) throws Exception {
        Map<String, Object> payload = validPayload(endpoint.entityType());

        mockMvc.perform(get(endpoint.path()))
                .andExpect(status().isOk());

        String createdJson = mockMvc.perform(post(endpoint.path())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long id = objectMapper.readTree(createdJson).get("id").asLong();

        mockMvc.perform(get(endpoint.path()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id));

        mockMvc.perform(get(endpoint.path() + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));

        payload.put("userNameLastChange", "updated-by-integration-test");
        mockMvc.perform(put(endpoint.path() + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.userNameLastChange").value("updated-by-integration-test"));

        mockMvc.perform(get(endpoint.path() + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userNameLastChange").value("updated-by-integration-test"));

        mockMvc.perform(delete(endpoint.path() + "/" + id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(endpoint.path() + "/" + id))
                .andExpect(status().isNotFound());

        mockMvc.perform(get(endpoint.path()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        entityManager.flush();
        entityManager.clear();
        BaseEntity deletedEntity = entityManager.find(endpoint.entityType(), id);
        assertNotNull(deletedEntity, "Soft delete must keep the row in the database");
        assertFalse(deletedEntity.getActive(), "Soft delete must set active=false");
    }

    @ParameterizedTest(name = "{0} returns 404 for missing records")
    @MethodSource("entityEndpoints")
    void returnsNotFoundForMissingRecords(Endpoint endpoint) throws Exception {
        long missingId = Long.MAX_VALUE;
        String emptyEntity = "{}";

        mockMvc.perform(get(endpoint.path() + "/" + missingId))
                .andExpect(status().isNotFound());

        mockMvc.perform(put(endpoint.path() + "/" + missingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(emptyEntity))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete(endpoint.path() + "/" + missingId))
                .andExpect(status().isNotFound());
    }

    @Test
    void publishesCrudOperationsForEveryEntityInOpenApiDocument() throws Exception {
        String document = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode paths = objectMapper.readTree(document).path("paths");

        for (Endpoint endpoint : entityEndpoints().toList()) {
            JsonNode collectionPath = paths.path(endpoint.path());
            JsonNode itemPath = paths.path(endpoint.path() + "/{id}");

            assertTrue(collectionPath.has("get"), endpoint.path() + " should document GET");
            assertTrue(collectionPath.has("post"), endpoint.path() + " should document POST");
            assertTrue(itemPath.has("get"), endpoint.path() + "/{id} should document GET");
            assertTrue(itemPath.has("put"), endpoint.path() + "/{id} should document PUT");
            assertTrue(itemPath.has("delete"), endpoint.path() + "/{id} should document DELETE");
            assertOperationHasEntityDocumentation(collectionPath.path("get"), endpoint.path() + " GET", endpoint.path());
            assertOperationHasEntityDocumentation(collectionPath.path("post"), endpoint.path() + " POST", endpoint.path());
            assertOperationHasEntityDocumentation(itemPath.path("get"), endpoint.path() + "/{id} GET", endpoint.path());
            assertOperationHasEntityDocumentation(itemPath.path("put"), endpoint.path() + "/{id} PUT", endpoint.path());
            assertOperationHasEntityDocumentation(itemPath.path("delete"), endpoint.path() + "/{id} DELETE", endpoint.path());
        }
        assertEquals(31, entityEndpoints().count());
    }

    private void assertOperationHasEntityDocumentation(JsonNode operation, String endpoint, String resourcePath) {
        assertTrue(operation.path("summary").isTextual(), endpoint + " should have a summary");
        JsonNode tags = operation.path("tags");
        assertTrue(tags.isArray() && !tags.isEmpty(),
                endpoint + " should be grouped by an entity tag");
        assertEquals(1, tags.size(), endpoint + " should belong to exactly one Swagger group");
        assertEquals(entityTagFor(resourcePath), tags.get(0).asText(),
                endpoint + " should be grouped under its entity tag");
        assertFalse(tags.toString().contains("\"CRUD\""),
                endpoint + " should not be grouped under the generic CRUD tag");
    }

    private String entityTagFor(String resourcePath) {
        return springContext.getBeansWithAnnotation(RestController.class).values().stream()
                .map(AopUtils::getTargetClass)
                .filter(controller -> controller.isAnnotationPresent(RequestMapping.class))
                .filter(controller -> controller.isAnnotationPresent(Tag.class))
                .filter(controller -> controller.getAnnotation(RequestMapping.class).value()[0].equals(resourcePath))
                .map(controller -> controller.getAnnotation(Tag.class).name())
                .findFirst()
                .orElseThrow(() -> new AssertionError("No entity Swagger tag found for " + resourcePath));
    }

    private Map<String, Object> validPayload(Class<? extends BaseEntity> entityType) {
        Map<String, Object> payload = new LinkedHashMap<>();
        for (Class<?> current = entityType; current != null && current != Object.class;
             current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (Modifier.isStatic(field.getModifiers()) || "id".equals(field.getName())) {
                    continue;
                }
                payload.put(field.getName(), "document".equals(field.getName())
                        ? null
                        : sampleValue(field.getType(), field.getName()));
            }
        }
        for (Class<?> current = entityType; current != null && current != Object.class;
             current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                Class<?> referenceType = field.getType() == Long.class
                        ? EntityRequestValidator.referenceType(entityType, field.getName())
                        : null;
                if (referenceType == null) {
                    continue;
                }

                Column column = field.getAnnotation(Column.class);
                boolean required = column != null && !column.nullable();
                payload.put(field.getName(), required ? seedEntity(referenceType) : null);
            }
        }
        payload.put("userNameCreator", "crud-integration-test");
        payload.put("userNameLastChange", "crud-integration-test");
        payload.put("creationDate", LocalDateTime.now().toString());
        payload.put("lastChangeDate", LocalDateTime.now().toString());
        return payload;
    }

    private Long seedEntity(Class<?> entityType) {
        Long existingId = seededReferenceIds.get(entityType);
        if (existingId != null) {
            return existingId;
        }

        try {
            BaseEntity entity = (BaseEntity) entityType.getDeclaredConstructor().newInstance();
            for (Class<?> current = entityType; current != null && current != Object.class;
                 current = current.getSuperclass()) {
                for (Field field : current.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) || "id".equals(field.getName())) {
                        continue;
                    }

                    field.setAccessible(true);
                    Class<?> referenceType = field.getType() == Long.class
                            ? EntityRequestValidator.referenceType(entityType, field.getName())
                            : null;
                    if (referenceType != null) {
                        Column column = field.getAnnotation(Column.class);
                        boolean required = column != null && !column.nullable();
                        field.set(entity, required ? seedEntity(referenceType) : null);
                    } else {
                        field.set(entity, sampleEntityValue(field.getType(), field.getName()));
                    }
                }
            }
            entityManager.persist(entity);
            entityManager.flush();
            seededReferenceIds.put(entityType, entity.getId());
            return entity.getId();
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Could not create fixture for " + entityType.getName(), exception);
        }
    }

    private Object sampleEntityValue(Class<?> fieldType, String fieldName) {
        if (fieldType == LocalDateTime.class) {
            return LocalDateTime.now();
        }
        if (fieldType.isEnum()) {
            return fieldType.getEnumConstants()[0];
        }
        if ("document".equals(fieldName)) {
            return null;
        }
        return sampleValue(fieldType, fieldName);
    }

    private Object sampleValue(Class<?> fieldType, String fieldName) {
        if (fieldType == String.class) {
            return "test-" + fieldName + "-" + UUID.randomUUID();
        }
        if (fieldType == Long.class || fieldType == long.class) {
            return 1L;
        }
        if (fieldType == Integer.class || fieldType == int.class) {
            return 1;
        }
        if (fieldType == Boolean.class || fieldType == boolean.class) {
            return true;
        }
        if (fieldType == BigDecimal.class) {
            return BigDecimal.ONE;
        }
        if (fieldType == LocalDateTime.class) {
            return LocalDateTime.now().toString();
        }
        if (fieldType.isEnum()) {
            return fieldType.getEnumConstants()[0].toString();
        }
        throw new IllegalArgumentException("Unsupported test fixture type: " + fieldType.getName());
    }

    private static Stream<Endpoint> entityEndpoints() {
        return Stream.of(
                new Endpoint(Categoria.class, "/api/categorias"),
                new Endpoint(Cliente.class, "/api/clientes"),
                new Endpoint(Embalagem.class, "/api/embalagens"),
                new Endpoint(Fornecedor.class, "/api/fornecedores"),
                new Endpoint(Produto.class, "/api/produtos"),
                new Endpoint(Segmento.class, "/api/segmentos"),
                new Endpoint(UnidadeMedida.class, "/api/unidades-medida"),
                new Endpoint(Armazem.class, "/api/armazens"),
                new Endpoint(CentroDistribuicao.class, "/api/centros-distribuicao"),
                new Endpoint(Estoque.class, "/api/estoques"),
                new Endpoint(LocalEstoque.class, "/api/locais-estoque"),
                new Endpoint(TipoLocal.class, "/api/tipos-local"),
                new Endpoint(ZonaArmazenagem.class, "/api/zonas-armazenagem"),
                new Endpoint(Lote.class, "/api/lotes"),
                new Endpoint(MovimentacaoEstoque.class, "/api/movimentacoes-estoque"),
                new Endpoint(Selo.class, "/api/selos"),
                new Endpoint(UnidadeLogistica.class, "/api/unidades-logisticas"),
                new Endpoint(Expedicao.class, "/api/expedicoes"),
                new Endpoint(Inventario.class, "/api/inventarios"),
                new Endpoint(OrdemEstoque.class, "/api/ordens-estoque"),
                new Endpoint(OrdemMovimentacao.class, "/api/ordens-movimentacao"),
                new Endpoint(Pedido.class, "/api/pedidos"),
                new Endpoint(Recebimento.class, "/api/recebimentos"),
                new Endpoint(Separacao.class, "/api/separacoes"),
                new Endpoint(MotivoMovimentacao.class, "/api/motivos-movimentacao"),
                new Endpoint(OrdemEstoqueItem.class, "/api/ordens-estoque-itens"),
                new Endpoint(InventarioItem.class, "/api/inventario-itens"),
                new Endpoint(PedidoItem.class, "/api/pedido-itens"),
                new Endpoint(RecebimentoItem.class, "/api/recebimento-itens"),
                new Endpoint(SeparacaoItem.class, "/api/separacao-itens"),
                new Endpoint(TransactionLog.class, "/api/logs-transacao"));
    }

    private record Endpoint(Class<? extends BaseEntity> entityType, String path) {
        @Override
        public String toString() {
            return entityType.getSimpleName();
        }
    }
}
