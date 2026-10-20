package com.wms.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.Main;
import com.wms.domain.auditoria.AuditLog;
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
import com.wms.domain.estoque.LocalEstoque;
import com.wms.domain.estoque.TipoLocal;
import com.wms.domain.estoque.ZonaArmazenagem;
import com.wms.domain.identificacao.Lote;
import com.wms.domain.identificacao.Selo;
import com.wms.domain.identificacao.UnidadeLogistica;
import com.wms.domain.operacoes.Expedicao;
import com.wms.domain.operacoes.Inventario;
import com.wms.domain.operacoes.OrdemEstoque;
import com.wms.domain.operacoes.OrdemMovimentacao;
import com.wms.domain.operacoes.Pedido;
import com.wms.domain.operacoes.Recebimento;
import com.wms.domain.operacoes.Separacao;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
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
    void stockMovementHistoryCannotBeCreatedUpdatedOrDeletedThroughItsApi() throws Exception {
        mockMvc.perform(get("/api/movimentacoes-estoque"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/movimentacoes-estoque/1"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/movimentacoes-estoque")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(put("/api/movimentacoes-estoque/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(delete("/api/movimentacoes-estoque/1"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void stockBalancesCanOnlyBeReadThroughTheirApi() throws Exception {
        mockMvc.perform(get("/api/estoques"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/estoques/1"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/estoques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(put("/api/estoques/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(delete("/api/estoques/1"))
                .andExpect(status().isMethodNotAllowed());
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
        if (endpoint.entityType() == SeparacaoItem.class) {
            mockMvc.perform(get(endpoint.path()))
                    .andExpect(status().isOk());
            mockMvc.perform(post(endpoint.path())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
            mockMvc.perform(put(endpoint.path() + "/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isMethodNotAllowed());
            mockMvc.perform(delete(endpoint.path() + "/1"))
                    .andExpect(status().isMethodNotAllowed());
            return;
        }
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
        if (endpoint.entityType() == Separacao.class) {
            payload.put("status", "CONCLUIDO");
        }
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

    @Test
    @Transactional
    void automaticallyAuditsCrudChangesAndExposesAuditAsReadOnly() throws Exception {
        Map<String, Object> payload = validPayload(Cliente.class);
        payload.put("code", "CLIENTE-AUDITORIA");
        String createdJson = mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long clientId = objectMapper.readTree(createdJson).get("id").asLong();

        mockMvc.perform(get("/api/logs-transacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entityName").value(Cliente.class.getName()))
                .andExpect(jsonPath("$[0].entityId").value(clientId))
                .andExpect(jsonPath("$[0].operation").value("CREATE"))
                .andExpect(jsonPath("$[0].userName").value(payload.get("userNameCreator")));

        payload.put("userNameLastChange", "auditoria-update-test");
        mockMvc.perform(put("/api/clientes/" + clientId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/clientes/" + clientId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/logs-transacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].operation").value("DELETE"))
                .andExpect(jsonPath("$[0].previousState").isNotEmpty())
                .andExpect(jsonPath("$[0].currentState").isNotEmpty())
                .andExpect(jsonPath("$[1].operation").value("UPDATE"))
                .andExpect(jsonPath("$[1].previousState").isNotEmpty())
                .andExpect(jsonPath("$[1].currentState").isNotEmpty());

        mockMvc.perform(post("/api/logs-transacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AuditLog())))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(put("/api/logs-transacao/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isMethodNotAllowed());
        mockMvc.perform(delete("/api/logs-transacao/1"))
                .andExpect(status().isMethodNotAllowed());
    }

    @ParameterizedTest(name = "{0} rejects blank required String fields on POST and PUT")
    @MethodSource("entityEndpoints")
    @Transactional
    void rejectsBlankRequiredStringFields(Endpoint endpoint) throws Exception {
        if (endpoint.entityType() == SeparacaoItem.class) {
            return;
        }
        Map<String, Object> validPayload = validPayload(endpoint.entityType());
        String createdJson = mockMvc.perform(post(endpoint.path())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPayload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long id = objectMapper.readTree(createdJson).get("id").asLong();

        List<String> requiredFields = requiredStringFields(endpoint.entityType());
        assertFalse(requiredFields.isEmpty(), endpoint.entityType().getSimpleName()
                + " should have required String fields, including inherited audit fields");
        for (String field : requiredFields) {
            for (String invalidValue : new String[]{"", "   \t", null}) {
                Map<String, Object> invalidPayload = new LinkedHashMap<>(validPayload);
                invalidPayload.put(field, invalidValue);

                mockMvc.perform(post(endpoint.path())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidPayload)))
                        .andExpect(status().isBadRequest());

                mockMvc.perform(put(endpoint.path() + "/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidPayload)))
                        .andExpect(status().isBadRequest());
            }
        }
    }

    @Test
    @Transactional
    void acceptsBlankOptionalStringField() throws Exception {
        for (String optionalValue : new String[]{"", "   "}) {
            Map<String, Object> payload = validPayload(LocalEstoque.class);
            payload.put("description", optionalValue);
            mockMvc.perform(post("/api/locais-estoque")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(payload)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.description").value(optionalValue));
        }
    }

    @Test
    @Transactional
    void receivingItemsCreatesAndAccumulatesPersistedStock() throws Exception {
        Map<String, Object> receiptPayload = validPayload(Recebimento.class);
        String createdReceipt = mockMvc.perform(post("/api/recebimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(receiptPayload)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        long receiptId = objectMapper.readTree(createdReceipt).get("id").asLong();

        Map<String, Object> payload = validPayload(RecebimentoItem.class);
        payload.put("receiptId", receiptId);

        String firstReceiptItem = mockMvc.perform(post("/api/recebimento-itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.locationId").value(payload.get("locationId")))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long receiptItemId = objectMapper.readTree(firstReceiptItem).get("id").asLong();

        Map<String, Object> secondPayload = new LinkedHashMap<>(payload);
        secondPayload.put("quantity", new BigDecimal("2.0000"));
        mockMvc.perform(post("/api/recebimento-itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondPayload)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/recebimentos/{id}", receiptId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(receiptId));
        mockMvc.perform(get("/api/recebimento-itens/{id}", receiptItemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(receiptItemId))
                .andExpect(jsonPath("$.receiptId").value(receiptId));
        mockMvc.perform(get("/api/estoques"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].productId").value(payload.get("productId")))
                .andExpect(jsonPath("$[0].locationId").value(payload.get("locationId")))
                .andExpect(jsonPath("$[0].quantity").value(3.0))
                .andExpect(jsonPath("$[0].quantityAvailable").value(3.0));
        mockMvc.perform(get("/api/movimentacoes-estoque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].movementType").value(containsInAnyOrder("ENTRADA", "ENTRADA")))
                .andExpect(jsonPath("$[*].productId").value(containsInAnyOrder(
                        ((Number) payload.get("productId")).intValue(), ((Number) payload.get("productId")).intValue())))
                .andExpect(jsonPath("$[*].destinationLocationId").value(containsInAnyOrder(
                        ((Number) payload.get("locationId")).intValue(), ((Number) payload.get("locationId")).intValue())));
        mockMvc.perform(get("/api/ordens-estoque"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].orderType").value(containsInAnyOrder("ENTRADA", "ENTRADA")))
                .andExpect(jsonPath("$[*].status").value(containsInAnyOrder("CONCLUIDO", "CONCLUIDO")));
        mockMvc.perform(get("/api/ordens-estoque-itens"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].productId").value(containsInAnyOrder(
                        ((Number) payload.get("productId")).intValue(),
                        ((Number) payload.get("productId")).intValue())));
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
                .andExpect(endpoint.entityType() == SeparacaoItem.class
                        ? status().isMethodNotAllowed() : status().isNotFound());

        mockMvc.perform(delete(endpoint.path() + "/" + missingId))
                .andExpect(endpoint.entityType() == SeparacaoItem.class
                        ? status().isMethodNotAllowed() : status().isNotFound());
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
        assertEquals(27, entityEndpoints().count());
        JsonNode auditCollectionPath = paths.path("/api/logs-transacao");
        JsonNode auditItemPath = paths.path("/api/logs-transacao/{id}");
        assertTrue(auditCollectionPath.has("get"));
        assertTrue(auditItemPath.has("get"));
        assertFalse(auditCollectionPath.has("post"));
        assertFalse(auditItemPath.has("put"));
        assertFalse(auditItemPath.has("delete"));

        JsonNode stockCollectionPath = paths.path("/api/estoques");
        JsonNode stockItemPath = paths.path("/api/estoques/{id}");
        assertTrue(stockCollectionPath.has("get"));
        assertFalse(stockCollectionPath.has("post"));
        assertTrue(stockItemPath.has("get"));
        assertFalse(stockItemPath.has("put"));
        assertFalse(stockItemPath.has("delete"));
    }

    @Test
    void documentsStockMovementHistoryAsReadOnly() throws Exception {
        String document = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode paths = objectMapper.readTree(document).path("paths");
        JsonNode collectionPath = paths.path("/api/movimentacoes-estoque");
        JsonNode itemPath = paths.path("/api/movimentacoes-estoque/{id}");

        assertTrue(collectionPath.has("get"));
        assertFalse(collectionPath.has("post"));
        assertTrue(itemPath.has("get"));
        assertFalse(itemPath.has("put"));
        assertFalse(itemPath.has("delete"));
    }

    @Test
    void documentsMovementReasonsAsAReadOnlyCatalog() throws Exception {
        String document = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode catalogPath = objectMapper.readTree(document).path("paths").path("/api/motivos-movimentacao");

        assertTrue(catalogPath.has("get"));
        assertFalse(catalogPath.has("post"));
        assertFalse(objectMapper.readTree(document).path("paths").has("/api/motivos-movimentacao/{id}"));
    }

    private void assertOperationHasEntityDocumentation(JsonNode operation, String endpoint, String resourcePath) {
        assertTrue(operation.path("summary").isTextual(), endpoint + " should have a summary");
        assertTrue(operation.path("responses").has("403"),
                endpoint + " should document the forbidden response");
        assertTrue(operation.path("responses").path("403").path("description").isTextual(),
                endpoint + " should describe the forbidden response");
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
        if (entityType == RecebimentoItem.class) {
            payload.put("locationId", seedEntity(LocalEstoque.class));
        }
        payload.put("userNameCreator", "crud-integration-test");
        payload.put("userNameLastChange", "crud-integration-test");
        payload.put("creationDate", LocalDateTime.now().toString());
        payload.put("lastChangeDate", LocalDateTime.now().toString());
        return payload;
    }

    private List<String> requiredStringFields(Class<?> entityType) {
        List<String> requiredFields = new ArrayList<>();
        for (Class<?> current = entityType; current != null && current != Object.class;
             current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                Column column = field.getAnnotation(Column.class);
                if (field.getType() == String.class && column != null && !column.nullable()) {
                    requiredFields.add(field.getName());
                }
            }
        }
        return requiredFields;
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
                new Endpoint(LocalEstoque.class, "/api/locais-estoque"),
                new Endpoint(TipoLocal.class, "/api/tipos-local"),
                new Endpoint(ZonaArmazenagem.class, "/api/zonas-armazenagem"),
                new Endpoint(Lote.class, "/api/lotes"),
                new Endpoint(Selo.class, "/api/selos"),
                new Endpoint(UnidadeLogistica.class, "/api/unidades-logisticas"),
                new Endpoint(Expedicao.class, "/api/expedicoes"),
                new Endpoint(Inventario.class, "/api/inventarios"),
                new Endpoint(OrdemEstoque.class, "/api/ordens-estoque"),
                new Endpoint(OrdemMovimentacao.class, "/api/ordens-movimentacao"),
                new Endpoint(Pedido.class, "/api/pedidos"),
                new Endpoint(Recebimento.class, "/api/recebimentos"),
                new Endpoint(Separacao.class, "/api/separacoes"),
                new Endpoint(OrdemEstoqueItem.class, "/api/ordens-estoque-itens"),
                new Endpoint(InventarioItem.class, "/api/inventario-itens"),
                new Endpoint(PedidoItem.class, "/api/pedido-itens"),
                new Endpoint(RecebimentoItem.class, "/api/recebimento-itens"),
                new Endpoint(SeparacaoItem.class, "/api/separacao-itens"));
    }

    private record Endpoint(Class<? extends BaseEntity> entityType, String path) {
        @Override
        public String toString() {
            return entityType.getSimpleName();
        }
    }
}
