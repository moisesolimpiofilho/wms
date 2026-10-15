package com.wms.service.estoque;

import com.wms.domain.enums.MovementType;
import com.wms.domain.enums.StatusOperacao;
import com.wms.domain.estoque.Estoque;
import com.wms.domain.auditoria.TransactionLog;
import com.wms.domain.identificacao.MovimentacaoEstoque;
import com.wms.domain.operacoes.Expedicao;
import com.wms.domain.operacoes.OrdemEstoque;
import com.wms.domain.operacoes.ajustes.MotivoMovimentacao;
import com.wms.integration.estoque.OrderMovementRequest;
import com.wms.repository.estoque.EstoqueRepository;
import com.wms.repository.identificacao.MovimentacaoEstoqueRepository;
import com.wms.repository.operacoes.ExpedicaoRepository;
import com.wms.repository.operacoes.OrdemEstoqueItemRepository;
import com.wms.repository.operacoes.OrdemEstoqueRepository;
import com.wms.service.cache.EstoqueCacheService;
import com.wms.service.transaction.RedisTransactionStore;
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
    private ExpedicaoRepository expedicaoRepository;
    @Mock
    private EstoqueRepository stockRepository;
    @Mock
    private EstoqueCacheService estoqueCacheService;
    @Mock
    private RedisTransactionStore transactionStore;

    private OrderMovementProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new OrderMovementProcessor(
                orderRepository, orderItemRepository, movementRepository, expedicaoRepository, stockRepository,
                estoqueCacheService, transactionStore);
    }

    @Test
    void processesInboundOrderAndCreatesAvailableStock() {
        OrderMovementRequest request = new OrderMovementRequest(
                "IN-001", MovementType.ENTRADA, 11L, 21L, 31L, null, new BigDecimal("10.0000"),
                new BigDecimal("2.5000"), MotivoMovimentacao.RECEBIMENTO_AQUISICAO, 61L,
                "integracao-externa", "Teste");
        when(orderRepository.findByCode(request.code())).thenReturn(Optional.empty());
        when(stockRepository.lockActiveByProductAndLocation(11L, 21L, 31L, null)).thenReturn(List.of());
        when(orderRepository.save(any(OrdemEstoque.class))).thenAnswer(invocation -> {
            OrdemEstoque order = invocation.getArgument(0);
            order.setId(41L);
            return order;
        });
        when(movementRepository.save(any(MovimentacaoEstoque.class))).thenAnswer(invocation -> {
            MovimentacaoEstoque movement = invocation.getArgument(0);
            movement.setId(51L);
            return movement;
        });

        OrdemEstoque order = processor.process(request);

        assertEquals(StatusOperacao.CONCLUIDO, order.getStatus());
        assertEquals(MovementType.ENTRADA, order.getOrderType());
        assertEquals(MotivoMovimentacao.RECEBIMENTO_AQUISICAO, order.getReason());
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
        verifyNoInteractions(expedicaoRepository);

        ArgumentCaptor<TransactionLog> transactionCaptor = ArgumentCaptor.forClass(TransactionLog.class);
        verify(transactionStore).save(transactionCaptor.capture());
        TransactionLog transaction = transactionCaptor.getValue();
        assertEquals(51L, transaction.getMovementId());
        assertEquals(61L, transaction.getUserId());
        assertEquals(11L, transaction.getProductId());
        assertEquals(21L, transaction.getBatchId());
        assertEquals(MotivoMovimentacao.RECEBIMENTO_AQUISICAO.getDescricao(), transaction.getReason());
        assertEquals(new BigDecimal("10.0000"), transaction.getQuantity());
        assertNull(transaction.getSealId());
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
        when(movementRepository.save(any(MovimentacaoEstoque.class))).thenAnswer(invocation -> {
            MovimentacaoEstoque movement = invocation.getArgument(0);
            movement.setId(52L);
            return movement;
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
        ArgumentCaptor<Expedicao> expedicaoCaptor = ArgumentCaptor.forClass(Expedicao.class);
        verify(expedicaoRepository).save(expedicaoCaptor.capture());
        Expedicao expedicao = expedicaoCaptor.getValue();
        assertEquals("EXP-OUT-001", expedicao.getCode());
        assertEquals(order.getId(), expedicao.getOrderId());
        assertEquals(StatusOperacao.CONCLUIDO, expedicao.getStatus());
        assertNotNull(expedicao.getShippingDate());
        assertEquals("ABC1D23", expedicao.getVehiclePlate());
        assertEquals("integracao-externa", expedicao.getUserNameCreator());
    }

    @Test
    void rejectsOutboundOrderWhenStockIsInsufficient() {
        OrderMovementRequest request = request(MovementType.SAIDA, "OUT-002", "6.0000");
        when(orderRepository.findByCode(request.code())).thenReturn(Optional.empty());
        when(stockRepository.lockActiveByProductAndLocation(11L, 21L, 31L, null))
                .thenReturn(List.of(stock("5.0000", "5.0000")));

        assertThrows(IllegalArgumentException.class, () -> processor.process(request));

        verify(orderRepository, never()).save(any(OrdemEstoque.class));
        verifyNoInteractions(orderItemRepository, movementRepository, expedicaoRepository);
        verify(stockRepository, never()).save(any(Estoque.class));
    }

    @Test
    void ignoresAlreadyProcessedOrderCode() {
        OrderMovementRequest request = request(MovementType.ENTRADA, "IN-001", "10.0000");
        OrdemEstoque existingOrder = new OrdemEstoque();
        existingOrder.setCode(request.code());
        when(orderRepository.findByCode(request.code())).thenReturn(Optional.of(existingOrder));

        assertSame(existingOrder, processor.process(request));

        verifyNoInteractions(stockRepository, orderItemRepository, movementRepository, expedicaoRepository);
        verify(orderRepository, never()).save(any(OrdemEstoque.class));
    }

    private OrderMovementRequest request(MovementType type, String code, String quantity) {
        return new OrderMovementRequest(
                code, type, 11L, 21L, 31L, null, new BigDecimal(quantity), null,
                type == MovementType.ENTRADA
                        ? MotivoMovimentacao.RECEBIMENTO_PRODUCAO
                        : MotivoMovimentacao.EXPEDICAO_VENDA,
                61L, "integracao-externa", "Teste",
                type == MovementType.SAIDA ? "ABC1D23" : null);
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
