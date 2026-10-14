package com.wms.api.integracao;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.integration.estoque.OrderMovementPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrdemEstoqueIntegrationControllerTest {
    private OrderMovementPublisher publisher;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        publisher = mock(OrderMovementPublisher.class);
        OrdemEstoqueIntegrationController controller = new OrdemEstoqueIntegrationController(
                new ObjectMapper().findAndRegisterModules(), publisher, "warehouse:orders");
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void publishesExternalOrderAndReturnsAccepted() throws Exception {
        when(publisher.publish(anyString())).thenReturn(1L);

        mockMvc.perform(post("/api/integracao/ordens-estoque")
                        .contentType(APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderCode").value("EXT-001"))
                .andExpect(jsonPath("$.topic").value("warehouse:orders"))
                .andExpect(jsonPath("$.subscribers").value(1));

        verify(publisher).publish(anyString());
    }

    @Test
    void doesNotAcceptOrderWhenNoRedisConsumerIsSubscribed() throws Exception {
        when(publisher.publish(anyString())).thenReturn(0L);

        mockMvc.perform(post("/api/integracao/ordens-estoque")
                        .contentType(APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void rejectsInvalidRequestBeforePublishing() throws Exception {
        mockMvc.perform(post("/api/integracao/ordens-estoque")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "EXT-001",
                                  "movementType": "SAIDA",
                                  "productId": 1,
                                  "locationId": 2,
                                  "reasonId": 3,
                                  "userId": 4,
                                  "userName": "external-app"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(publisher);
    }

    private String validRequest() {
        return """
                {
                  "code": "EXT-001",
                  "movementType": "ENTRADA",
                  "productId": 1,
                  "locationId": 2,
                  "quantity": 1,
                  "reasonId": 3,
                  "userId": 4,
                  "userName": "external-app"
                }
                """;
    }
}
