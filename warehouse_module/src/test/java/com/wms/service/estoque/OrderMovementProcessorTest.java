package com.wms.service.estoque;

import com.wms.domain.enums.MovementType;
import com.wms.domain.enums.StatusOperacao;
import com.wms.domain.estoque.Estoque;
import com.wms.domain.identificacao.MovimentacaoEstoque;
import com.wms.domain.operacoes.OrdemEstoque;
import com.wms.integration.estoque.OrderMovementRequest;
import com.wms.repository.estoque.EstoqueRepository;
import com.wms.repository.identificacao.MovimentacaoEstoqueRepository;
import com.wms.repository.operacoes.OrdemEstoqueItemRepository;
import com.wms.repository.operacoes.OrdemEstoqueRepository;
import com.wms.service.cache.EstoqueCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderMovementProcessorTest {
    @Mock
    private OrdemEstoqueRepository orderRepository;
    @Mock
    private OrdemEstoqueItemRepository orderItemRepository;
    @Mock
    private MovimentacaoEstoqueRepository movementRepository;
    @Mock
    private EstoqueRepository stockRepository;
    @Mock
    private EstoqueCacheService estoqueCacheService;

    private OrderMovementProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new OrderMovementProcessor(
                orderRepository, orderItemRepository, movementRepository, stockRepository, estoqueCacheService);
    }

    @Test
    void processesInboundOrderAndCreatesAvailableStock() {
        OrderMovementRequest request = new OrderMovementRequest(
                "IN-001", MovementType.ENTRADA, 11L, 21L, 31L, null, new BigDecimal("10.0000"),
                new BigDecimal("2.5000"), 51L, 61L, "integracao-externa", "Teste");
        when(orderRepository.findByCode(request.code())).thenReturn(Optional.empty());
        when(stockRepository.lockActiveByProductAndLocation(11L, 21L, 31L, null)).thenReturn(List.of());
        when(orderRepository.save(any(OrdemEstoque.class))).thenAnswer(invocation -> {
            OrdemEstoque order = invocation.getArgument(0);
            order.setId(41L);
            return order;
        });

        OrdemEstoque order = processor.process(request);

        assertEquals(StatusOperacao.CONCLUIDO, order.getStatus());
        assertEquals(MovementType.ENTRADA, order.getOrderType());
        assertEquals(31L, order.getDestinationLocationId());

        ArgumentCaptor<Estoque> stockCaptor = ArgumentCaptor.forClass(Estoque.class);
        verify(stockRepository).save(stockCaptor.capture());
        assertEquals(new BigDecimal("10.0000"), stockCaptor.getValue().getQuantity());
        assertEquals(new BigDecimal("10.0000"), stockCaptor.getValue().getQuantityAvailable());
        assertEquals(new BigDecimal("2.5000"), stockCaptor.getValue().getWeight());

        ArgumentCaptor<MovimentacaoEstoque> movementCaptor =
                ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movementRepository).save(movementCaptor.capture());
        assertEquals(41L, movementCaptor.getValue().getOrderId());
        assertEquals(31L, movementCaptor.getValue().getDestinationLocationId());
        assertNull(movementCaptor.getValue().getOriginLocationId());
    }

    @Test
    void processesOutboundOrderAndDeductsAvailableStock() {
        OrderMovementRequest request = request(MovementType.SAIDA, "OUT-001", "6.0000");
        Estoque stock = stock("4.0000", "4.0000");
        Estoque secondStock = stock("5.0000", "5.0000");
        when(orderRepository.findByCode(request.code())).thenReturn(Optional.empty());
        when(stockRepository.lockActiveByProductAndLocation(11L, 21L, 31L, null))
                .thenReturn(List.of(stock, secondStock));
        when(orderRepository.save(any(OrdemEstoque.class))).thenAnswer(invocation -> {
            OrdemEstoque order = invocation.getArgument(0);
            order.setId(42L);
            return order;
        });

        OrdemEstoque order = processor.process(request);

        assertEquals(31L, order.getOriginLocationId());
        assertEquals(new BigDecimal("0.0000"), stock.getQuantityAvailable());
        assertEquals(new BigDecimal("3.0000"), secondStock.getQuantityAvailable());
        verify(stockRepository, times(2)).save(any(Estoque.class));
        verify(movementRepository).save(argThat(movement ->
                movement.getMovementType() == MovementType.SAIDA
                        && movement.getOriginLocationId().equals(31L)
                        && movement.getDestinationLocationId() == null));
    }

    @Test
    void rejectsOutboundOrderWhenStockIsInsufficient() {
        OrderMovementRequest request = request(MovementType.SAIDA, "OUT-002", "6.0000");
        when(orderRepository.findByCode(request.code())).thenReturn(Optional.empty());
        when(stockRepository.lockActiveByProductAndLocation(11L, 21L, 31L, null))
                .thenReturn(List.of(stock("5.0000", "5.0000")));

        assertThrows(IllegalArgumentException.class, () -> processor.process(request));

        verify(orderRepository, never()).save(any(OrdemEstoque.class));
        verifyNoInteractions(orderItemRepository, movementRepository);
        verify(stockRepository, never()).save(any(Estoque.class));
    }

    @Test
    void ignoresAlreadyProcessedOrderCode() {
        OrderMovementRequest request = request(MovementType.ENTRADA, "IN-001", "10.0000");
        OrdemEstoque existingOrder = new OrdemEstoque();
        existingOrder.setCode(request.code());
        when(orderRepository.findByCode(request.code())).thenReturn(Optional.of(existingOrder));

        assertSame(existingOrder, processor.process(request));

        verifyNoInteractions(stockRepository, orderItemRepository, movementRepository);
        verify(orderRepository, never()).save(any(OrdemEstoque.class));
    }

    private OrderMovementRequest request(MovementType type, String code, String quantity) {
        return new OrderMovementRequest(
                code, type, 11L, 21L, 31L, null, new BigDecimal(quantity), null,
                51L, 61L, "integracao-externa", "Teste");
    }

    private Estoque stock(String quantity, String available) {
        Estoque stock = new Estoque();
        stock.setProductId(11L);
        stock.setBatchId(21L);
        stock.setLocationId(31L);
        stock.setQuantity(new BigDecimal(quantity));
        stock.setQuantityAvailable(new BigDecimal(available));
        stock.setQuantityReserved(BigDecimal.ZERO);
        stock.setWeight(BigDecimal.ZERO);
        stock.setActive(true);
        stock.setUserNameCreator("operador");
        stock.setUserNameLastChange("operador");
        return stock;
    }
}
