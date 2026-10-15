package com.wms.api.operacoes;

import com.wms.domain.base.BaseEntity;
import com.wms.domain.enums.MovementType;
import com.wms.domain.operacoes.ajustes.MotivoMovimentacao;
import com.wms.integration.estoque.OrderMovementRequest;
import com.wms.service.auditoria.CrudAuditService;
import com.wms.service.estoque.OrderMovementProcessor;
import com.wms.service.estoque.RecebimentoEstoqueService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RecebimentoItemControllerTest {
    private EntityManager entityManager;
    private OrderMovementProcessor processor;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        entityManager = mock(EntityManager.class);
        processor = mock(OrderMovementProcessor.class);
        RecebimentoEstoqueService recebimentoEstoqueService =
                new RecebimentoEstoqueService(processor);
        doAnswer(invocation -> {
            BaseEntity entity = invocation.getArgument(0);
            entity.setId(41L);
            return null;
        }).when(entityManager).persist(any());
        doAnswer(invocation -> new Object()).when(entityManager).find(any(), any());

        RecebimentoItemController controller =
                new RecebimentoItemController(entityManager, recebimentoEstoqueService);
        ReflectionTestUtils.setField(controller, "auditService", mock(CrudAuditService.class));
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void createsInboundOrderWhenReceiptItemIsRegistered() throws Exception {
        mockMvc.perform(post("/api/recebimento-itens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiptId": 1,
                                  "productId": 2,
                                  "batchId": 3,
                                  "locationId": 4,
                                  "quantity": 6,
                                  "userId": 5,
                                  "reason": "RECEBIMENTO_AQUISICAO",
                                  "userNameCreator": "recebimento-teste",
                                  "userNameLastChange": "recebimento-teste"
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<OrderMovementRequest> requestCaptor =
                ArgumentCaptor.forClass(OrderMovementRequest.class);
        verify(processor).process(requestCaptor.capture());
        OrderMovementRequest request = requestCaptor.getValue();
        assertEquals("RECEBIMENTO-1-41", request.code());
        assertEquals(MovementType.ENTRADA, request.movementType());
        assertEquals(2L, request.productId());
        assertEquals(3L, request.batchId());
        assertEquals(4L, request.locationId());
        assertEquals(5L, request.userId());
        assertEquals(MotivoMovimentacao.RECEBIMENTO_AQUISICAO, request.reason());
        assertEquals(6, request.quantity().intValue());
    }
}
