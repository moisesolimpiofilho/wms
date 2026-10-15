package com.wms.service.estoque;

import com.wms.domain.enums.MovementType;
import com.wms.domain.operacoes.ajustes.MotivoMovimentacao;
import com.wms.domain.operacoes.itens.RecebimentoItem;
import com.wms.integration.estoque.OrderMovementRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RecebimentoEstoqueServiceTest {
    @Test
    void convertsReceiptItemToInboundOrderMovement() {
        OrderMovementProcessor processor = mock(OrderMovementProcessor.class);
        RecebimentoEstoqueService service = new RecebimentoEstoqueService(processor);
        RecebimentoItem item = receivedItem();

        service.registrarEntrada(item);

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(OrderMovementRequest.class);
        verify(processor).process(requestCaptor.capture());
        OrderMovementRequest request = requestCaptor.getValue();
        assertEquals("RECEBIMENTO-7-41", request.code());
        assertEquals(MovementType.ENTRADA, request.movementType());
        assertEquals(11L, request.productId());
        assertEquals(21L, request.batchId());
        assertEquals(31L, request.locationId());
        assertEquals(new BigDecimal("8.0000"), request.quantity());
        assertEquals(new BigDecimal("1.5000"), request.weight());
        assertEquals(MotivoMovimentacao.RECEBIMENTO_AQUISICAO, request.reason());
        assertEquals(61L, request.userId());
        assertEquals("recebimento-teste", request.userName());
    }

    @Test
    void rejectsNonReceivingReason() {
        OrderMovementProcessor processor = mock(OrderMovementProcessor.class);
        RecebimentoEstoqueService service = new RecebimentoEstoqueService(processor);
        RecebimentoItem item = receivedItem();
        item.setReason(MotivoMovimentacao.EXPEDICAO_VENDA);

        assertThrows(IllegalArgumentException.class, () -> service.registrarEntrada(item));
    }

    private RecebimentoItem receivedItem() {
        RecebimentoItem item = new RecebimentoItem();
        item.setId(41L);
        item.setReceiptId(7L);
        item.setProductId(11L);
        item.setBatchId(21L);
        item.setLocationId(31L);
        item.setQuantity(new BigDecimal("8.0000"));
        item.setWeight(new BigDecimal("1.5000"));
        item.setUserId(61L);
        item.setReason(MotivoMovimentacao.RECEBIMENTO_AQUISICAO);
        item.setUserNameLastChange("recebimento-teste");
        return item;
    }
}
