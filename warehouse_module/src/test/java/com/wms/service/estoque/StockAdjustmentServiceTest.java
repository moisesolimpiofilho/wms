package com.wms.service.estoque;

import com.wms.domain.auditoria.TransactionLog;
import com.wms.domain.enums.MovementType;
import com.wms.domain.enums.StatusOperacao;
import com.wms.domain.estoque.Estoque;
import com.wms.domain.identificacao.MovimentacaoEstoque;
import com.wms.domain.operacoes.Inventario;
import com.wms.domain.operacoes.ajustes.HistoricoAjuste;
import com.wms.domain.operacoes.ajustes.MotivoMovimentacao;
import com.wms.domain.operacoes.itens.InventarioItem;
import com.wms.integration.estoque.StockAdjustmentRequest;
import com.wms.repository.estoque.EstoqueRepository;
import com.wms.repository.identificacao.MovimentacaoEstoqueRepository;
import com.wms.repository.operacoes.HistoricoAjusteRepository;
import com.wms.repository.operacoes.InventarioItemRepository;
import com.wms.repository.operacoes.InventarioRepository;
import com.wms.service.cache.EstoqueCacheService;
import com.wms.service.transaction.RedisTransactionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockAdjustmentServiceTest {
    @Mock
    private InventarioItemRepository inventoryItemRepository;
    @Mock
    private InventarioRepository inventoryRepository;
    @Mock
    private EstoqueRepository stockRepository;
    @Mock
    private MovimentacaoEstoqueRepository movementRepository;
    @Mock
    private HistoricoAjusteRepository historyRepository;
    @Mock
    private EstoqueCacheService stockCacheService;
    @Mock
    private RedisTransactionStore transactionStore;

    private StockAdjustmentService service;

    @BeforeEach
    void setUp() {
        service = new StockAdjustmentService(
                inventoryItemRepository, inventoryRepository, stockRepository, movementRepository,
                historyRepository, stockCacheService, transactionStore);
    }

    @Test
    void appliesSurplusAdjustmentAndRecordsHistoryMovementAndTransaction() {
        InventarioItem item = inventoryItem();
        Estoque stock = stock("10.0000", "8.0000", "2.0000", "4.0000");
        StockAdjustmentRequest request = request("12.0000", "5.0000");
        when(inventoryItemRepository.findActiveByIdForUpdate(11L)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(inventory()));
        when(stockRepository.lockActiveByProductAndLocation(21L, 31L, 41L, null))
                .thenReturn(List.of(stock));
        when(stockRepository.save(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementRepository.save(any(MovimentacaoEstoque.class))).thenAnswer(invocation -> {
            MovimentacaoEstoque movement = invocation.getArgument(0);
            movement.setId(51L);
            return movement;
        });
        when(historyRepository.save(any(HistoricoAjuste.class))).thenAnswer(invocation -> {
            HistoricoAjuste history = invocation.getArgument(0);
            history.setId(61L);
            return history;
        });

        StockAdjustmentService.AdjustmentResult result = service.adjust(11L, request);

        assertTrue(result.adjusted());
        assertEquals("SOBRA", result.adjustmentType());
        assertEquals(new BigDecimal("2.0000"), result.differenceQuantity());
        assertEquals(new BigDecimal("1.0000"), result.differenceWeight());
        assertEquals(61L, result.historyId());
        assertEquals(new BigDecimal("12.0000"), stock.getQuantity());
        assertEquals(new BigDecimal("10.0000"), stock.getQuantityAvailable());
        assertEquals(new BigDecimal("5.0000"), stock.getWeight());
        assertEquals(new BigDecimal("12.0000"), item.getQuantityCounted());
        assertEquals(new BigDecimal("5.0000"), item.getWeightCounted());

        ArgumentCaptor<MovimentacaoEstoque> movementCaptor =
                ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movementRepository).save(movementCaptor.capture());
        MovimentacaoEstoque movement = movementCaptor.getValue();
        assertEquals(MovementType.AJUSTE, movement.getMovementType());
        assertEquals(new BigDecimal("2.0000"), movement.getQuantity());
        assertEquals(new BigDecimal("1.0000"), movement.getWeight());
        assertEquals(41L, movement.getOriginLocationId());

        ArgumentCaptor<HistoricoAjuste> historyCaptor = ArgumentCaptor.forClass(HistoricoAjuste.class);
        verify(historyRepository).save(historyCaptor.capture());
        assertEquals(new BigDecimal("10.0000"), historyCaptor.getValue().getSystemQuantity());
        assertEquals(new BigDecimal("12.0000"), historyCaptor.getValue().getCountedQuantity());

        ArgumentCaptor<TransactionLog> transactionCaptor = ArgumentCaptor.forClass(TransactionLog.class);
        verify(transactionStore).save(transactionCaptor.capture());
        assertEquals(51L, transactionCaptor.getValue().getMovementId());
        assertEquals(71L, transactionCaptor.getValue().getUserId());
        assertEquals(MovementType.AJUSTE, transactionCaptor.getValue().getMovementType());
        verify(stockCacheService).cache(stock);
    }

    @Test
    void doesNotCreateMovementWhenStockAlreadyMatchesTheCount() {
        InventarioItem item = inventoryItem();
        StockAdjustmentRequest request = request("10.0000", null);
        when(inventoryItemRepository.findActiveByIdForUpdate(11L)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(inventory()));
        when(stockRepository.lockActiveByProductAndLocation(21L, 31L, 41L, null))
                .thenReturn(List.of(stock("10.0000", "8.0000", "2.0000", "4.0000")));

        StockAdjustmentService.AdjustmentResult result = service.adjust(11L, request);

        assertFalse(result.adjusted());
        verifyNoInteractions(movementRepository, historyRepository, transactionStore, stockCacheService);
        verify(stockRepository, never()).save(any(Estoque.class));
    }

    @Test
    void appliesLossWithoutReducingReservedQuantity() {
        InventarioItem item = inventoryItem();
        Estoque stock = stock("10.0000", "8.0000", "2.0000", "4.0000");
        StockAdjustmentRequest request = request("6.0000", null);
        when(inventoryItemRepository.findActiveByIdForUpdate(11L)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(inventory()));
        when(stockRepository.lockActiveByProductAndLocation(21L, 31L, 41L, null))
                .thenReturn(List.of(stock));
        when(stockRepository.save(any(Estoque.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movementRepository.save(any(MovimentacaoEstoque.class))).thenAnswer(invocation -> {
            MovimentacaoEstoque movement = invocation.getArgument(0);
            movement.setId(51L);
            return movement;
        });
        when(historyRepository.save(any(HistoricoAjuste.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockAdjustmentService.AdjustmentResult result = service.adjust(11L, request);

        assertTrue(result.adjusted());
        assertEquals("PERDA", result.adjustmentType());
        assertEquals(new BigDecimal("4.0000"), result.differenceQuantity());
        assertEquals(new BigDecimal("6.0000"), stock.getQuantity());
        assertEquals(new BigDecimal("4.0000"), stock.getQuantityAvailable());
        assertEquals(new BigDecimal("2.0000"), stock.getQuantityReserved());
        verify(stockCacheService).cache(stock);
    }

    @Test
    void rejectsCountBelowReservedBalanceWithoutChangingStock() {
        InventarioItem item = inventoryItem();
        StockAdjustmentRequest request = request("1.0000", null);
        when(inventoryItemRepository.findActiveByIdForUpdate(11L)).thenReturn(Optional.of(item));
        when(inventoryRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(inventory()));
        when(stockRepository.lockActiveByProductAndLocation(21L, 31L, 41L, null))
                .thenReturn(List.of(stock("10.0000", "8.0000", "2.0000", "4.0000")));

        assertThrows(ResponseStatusException.class, () -> service.adjust(11L, request));

        verify(stockRepository, never()).save(any(Estoque.class));
        verifyNoInteractions(movementRepository, historyRepository, transactionStore, stockCacheService);
    }

    private InventarioItem inventoryItem() {
        InventarioItem item = new InventarioItem();
        item.setId(11L);
        item.setInventoryId(1L);
        item.setProductId(21L);
        item.setBatchId(31L);
        item.setLocationId(41L);
        return item;
    }

    private Inventario inventory() {
        Inventario inventory = new Inventario();
        inventory.setId(1L);
        inventory.setStatus(StatusOperacao.EM_CONTAGEM);
        return inventory;
    }

    private Estoque stock(String quantity, String available, String reserved, String weight) {
        Estoque stock = new Estoque();
        stock.setProductId(21L);
        stock.setBatchId(31L);
        stock.setLocationId(41L);
        stock.setQuantity(new BigDecimal(quantity));
        stock.setQuantityAvailable(new BigDecimal(available));
        stock.setQuantityReserved(new BigDecimal(reserved));
        stock.setWeight(new BigDecimal(weight));
        return stock;
    }

    private StockAdjustmentRequest request(String quantity, String weight) {
        return new StockAdjustmentRequest(
                21L,
                41L,
                31L,
                new BigDecimal(quantity),
                weight == null ? null : new BigDecimal(weight),
                MotivoMovimentacao.AJUSTE_INVENTARIO,
                71L,
                "contagem-operador");
    }
}
