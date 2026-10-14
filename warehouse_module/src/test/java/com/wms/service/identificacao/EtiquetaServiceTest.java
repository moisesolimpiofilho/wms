package com.wms.service.identificacao;

import com.wms.domain.enums.MovementType;
import com.wms.domain.identificacao.MovimentacaoEstoque;
import com.wms.domain.identificacao.Selo;
import com.wms.repository.identificacao.MovimentacaoEstoqueRepository;
import com.wms.repository.identificacao.SeloRepository;
import com.wms.service.transaction.RedisTransactionStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EtiquetaServiceTest {
    private static final String CODE = "ETQ-0001";
    private static final String USER_NAME = "operador";
    private static final Long USER_ID = 7L;
    private static final Long PRODUCT_ID = 21L;
    private static final Long LOCATION_ID = 4L;

    @Mock
    private SeloRepository seloRepository;
    @Mock
    private MovimentacaoEstoqueRepository movimentacaoRepository;
    @Mock
    private RedisTransactionStore transactionStore;

    private EtiquetaService service;

    @BeforeEach
    void setUp() {
        service = new EtiquetaService(seloRepository, movimentacaoRepository, transactionStore);
    }

    @Test
    void registersEntryWithInitialAvailableQuantitiesAndMovement() {
        when(seloRepository.findByCode(CODE)).thenReturn(Optional.empty());
        when(seloRepository.save(any(Selo.class))).thenAnswer(invocation -> {
            Selo selo = invocation.getArgument(0);
            selo.setId(101L);
            return selo;
        });
        when(movimentacaoRepository.save(any(MovimentacaoEstoque.class))).thenAnswer(invocation -> {
            MovimentacaoEstoque movement = invocation.getArgument(0);
            movement.setId(201L);
            return movement;
        });

        Selo result = service.registrarEntrada(
                CODE, PRODUCT_ID, 31L, LOCATION_ID, new BigDecimal("12.5000"),
                new BigDecimal("2.2500"), USER_ID, USER_NAME);

        assertEquals(CODE, result.getCode());
        assertEquals(PRODUCT_ID, result.getProductId());
        assertEquals(new BigDecimal("12.5000"), result.getQuantity());
        assertEquals(new BigDecimal("12.5000"), result.getQuantityAvailable());
        assertEquals(BigDecimal.ZERO, result.getQuantityReserved());
        assertEquals(BigDecimal.ZERO, result.getQuantityCanceled());
        assertEquals(LOCATION_ID, result.getLocationId());
        assertTrue(result.getActive());

        ArgumentCaptor<MovimentacaoEstoque> movementCaptor =
                ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movimentacaoRepository).save(movementCaptor.capture());
        MovimentacaoEstoque movement = movementCaptor.getValue();
        assertEquals(MovementType.ENTRADA, movement.getMovementType());
        assertEquals(PRODUCT_ID, movement.getProductId());
        assertEquals(LOCATION_ID, movement.getDestinationLocationId());
        assertNull(movement.getOriginLocationId());
        assertEquals(new BigDecimal("12.5000"), movement.getQuantity());
        assertEquals(USER_NAME, movement.getUserName());

        verify(transactionStore).save(argThat(transaction ->
                transaction.getMovementId().equals(201L)
                        && transaction.getSealId().equals(101L)
                        && transaction.getMovementType() == MovementType.ENTRADA
                        && transaction.getQuantity().equals(new BigDecimal("12.5000"))
                        && transaction.getUserId().equals(USER_ID)));
    }

    @Test
    void rejectsEntryWhenLabelCodeAlreadyExists() {
        when(seloRepository.findByCode(CODE)).thenReturn(Optional.of(selo()));

        assertThrows(IllegalArgumentException.class, () -> service.registrarEntrada(
                CODE, PRODUCT_ID, null, LOCATION_ID, BigDecimal.ONE, null, USER_ID, USER_NAME));

        verify(seloRepository, never()).save(any(Selo.class));
        verifyNoInteractions(movimentacaoRepository, transactionStore);
    }

    @Test
    void rejectsEntryWithZeroOrNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> service.registrarEntrada(
                CODE, PRODUCT_ID, null, LOCATION_ID, BigDecimal.ZERO, null, USER_ID, USER_NAME));
        assertThrows(IllegalArgumentException.class, () -> service.registrarEntrada(
                CODE, PRODUCT_ID, null, LOCATION_ID, new BigDecimal("-1"), null, USER_ID, USER_NAME));

        verifyNoInteractions(seloRepository, movimentacaoRepository, transactionStore);
    }

    @Test
    void shipsPartOfLabelBalanceAndKeepsLabelActive() {
        Selo selo = selo();
        when(seloRepository.findByCode(CODE)).thenReturn(Optional.of(selo));
        when(seloRepository.save(any(Selo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimentacaoRepository.save(any(MovimentacaoEstoque.class))).thenAnswer(invocation -> {
            MovimentacaoEstoque movement = invocation.getArgument(0);
            movement.setId(202L);
            return movement;
        });

        Selo result = service.registrarSaida(CODE, new BigDecimal("3.2500"), USER_ID, USER_NAME);

        assertEquals(new BigDecimal("10.0000"), result.getQuantity());
        assertEquals(new BigDecimal("6.7500"), result.getQuantityAvailable());
        assertTrue(result.getActive());
        assertEquals("recebedor", result.getUserNameCreator());
        assertEquals(LocalDateTime.of(2026, 10, 7, 9, 0), result.getCreationDate());
        assertEquals(USER_NAME, result.getUserNameLastChange());

        ArgumentCaptor<MovimentacaoEstoque> movementCaptor =
                ArgumentCaptor.forClass(MovimentacaoEstoque.class);
        verify(movimentacaoRepository).save(movementCaptor.capture());
        MovimentacaoEstoque movement = movementCaptor.getValue();
        assertEquals(MovementType.SAIDA, movement.getMovementType());
        assertEquals(LOCATION_ID, movement.getOriginLocationId());
        assertNull(movement.getDestinationLocationId());
        assertEquals(new BigDecimal("3.2500"), movement.getQuantity());
        assertEquals(USER_NAME, movement.getUserName());

        verify(transactionStore).save(argThat(transaction ->
                transaction.getMovementId().equals(202L)
                        && transaction.getSealId().equals(101L)
                        && transaction.getMovementType() == MovementType.SAIDA
                        && transaction.getQuantity().equals(new BigDecimal("3.2500"))
                        && transaction.getUserId().equals(USER_ID)));
    }

    @Test
    void deactivatesLabelWhenLastAvailableQuantityIsShipped() {
        Selo selo = selo();
        when(seloRepository.findByCode(CODE)).thenReturn(Optional.of(selo));
        when(seloRepository.save(any(Selo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimentacaoRepository.save(any(MovimentacaoEstoque.class))).thenAnswer(invocation -> {
            MovimentacaoEstoque movement = invocation.getArgument(0);
            movement.setId(203L);
            return movement;
        });

        Selo result = service.registrarSaida(CODE, new BigDecimal("10.0000"), USER_ID, USER_NAME);

        assertEquals(new BigDecimal("0.0000"), result.getQuantityAvailable());
        assertFalse(result.getActive());
        verify(seloRepository).save(selo);
        verify(transactionStore).save(any());
    }

    @Test
    void rejectsExitGreaterThanAvailableQuantityWithoutWritingAnything() {
        when(seloRepository.findByCode(CODE)).thenReturn(Optional.of(selo()));

        assertThrows(IllegalArgumentException.class, () -> service.registrarSaida(
                CODE, new BigDecimal("10.0001"), USER_ID, USER_NAME));

        verify(seloRepository, never()).save(any(Selo.class));
        verifyNoInteractions(movimentacaoRepository, transactionStore);
    }

    @Test
    void rejectsExitWithZeroOrNegativeQuantity() {
        assertThrows(IllegalArgumentException.class, () -> service.registrarSaida(
                CODE, BigDecimal.ZERO, USER_ID, USER_NAME));
        assertThrows(IllegalArgumentException.class, () -> service.registrarSaida(
                CODE, new BigDecimal("-1"), USER_ID, USER_NAME));

        verifyNoInteractions(seloRepository, movimentacaoRepository, transactionStore);
    }

    @Test
    void rejectsExitForInactiveLabel() {
        Selo selo = selo();
        selo.setActive(false);
        when(seloRepository.findByCode(CODE)).thenReturn(Optional.of(selo));

        assertThrows(IllegalStateException.class, () -> service.registrarSaida(
                CODE, BigDecimal.ONE, USER_ID, USER_NAME));

        verify(seloRepository, never()).save(any(Selo.class));
        verifyNoInteractions(movimentacaoRepository, transactionStore);
    }

    @Test
    void looksUpLabelByCode() {
        Selo selo = selo();
        when(seloRepository.findByCode(CODE)).thenReturn(Optional.of(selo));

        assertEquals(Optional.of(selo), service.consultarPorCodigo(CODE));

        verify(seloRepository).findByCode(CODE);
        verifyNoInteractions(movimentacaoRepository, transactionStore);
    }

    @Test
    void returnsEmptyWhenLabelDoesNotExist() {
        when(seloRepository.findByCode(CODE)).thenReturn(Optional.empty());

        assertTrue(service.consultarPorCodigo(CODE).isEmpty());
    }

    @Test
    void rejectsBlankCodeForLookup() {
        assertThrows(IllegalArgumentException.class, () -> service.consultarPorCodigo(" "));

        verifyNoInteractions(seloRepository, movimentacaoRepository, transactionStore);
    }

    private Selo selo() {
        Selo selo = new Selo();
        selo.setId(101L);
        selo.setCode(CODE);
        selo.setProductId(PRODUCT_ID);
        selo.setBatchId(31L);
        selo.setLocationId(LOCATION_ID);
        selo.setQuantity(new BigDecimal("10.0000"));
        selo.setQuantityReserved(BigDecimal.ZERO);
        selo.setQuantityCanceled(BigDecimal.ZERO);
        selo.setQuantityAvailable(new BigDecimal("10.0000"));
        selo.setActive(true);
        selo.setUserNameCreator("recebedor");
        selo.setUserNameLastChange("recebedor");
        selo.setCreationDate(LocalDateTime.of(2026, 10, 7, 9, 0));
        return selo;
    }
}
