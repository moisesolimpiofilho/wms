package com.wms.service.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.domain.auditoria.TransactionLog;
import com.wms.domain.enums.MovementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisTransactionStoreTest {
    private static final String TRANSACTION_ID = "tx-2026-001";
    private static final String TRANSACTION_KEY = "warehouse:transactions:" + TRANSACTION_ID;

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;

    private ObjectMapper objectMapper;
    private RedisTransactionStore store;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        store = new RedisTransactionStore(redisTemplate, objectMapper);
    }

    @Test
    void storesTransactionWithoutExpiration() throws Exception {
        TransactionLog transaction = transaction();
        String json = objectMapper.writeValueAsString(transaction);
        when(valueOperations.setIfAbsent(TRANSACTION_KEY, json)).thenReturn(true);

        store.save(transaction);

        verify(valueOperations).setIfAbsent(TRANSACTION_KEY, json);
        verify(valueOperations, never()).set(anyString(), anyString(), any(java.time.Duration.class));
    }

    @Test
    void rejectsTransactionIdAlreadyInRedis() {
        when(valueOperations.setIfAbsent(eq(TRANSACTION_KEY), anyString())).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> store.save(transaction()));
    }

    @Test
    void reportsUnconfirmedRedisWrite() {
        when(valueOperations.setIfAbsent(eq(TRANSACTION_KEY), anyString())).thenReturn(null);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> store.save(transaction()));

        assertTrue(exception.getMessage().contains("Redis não confirmou"));
    }

    @Test
    void retrievesStoredTransactionByTransactionId() throws Exception {
        TransactionLog transaction = transaction();
        when(valueOperations.get(TRANSACTION_KEY)).thenReturn(objectMapper.writeValueAsString(transaction));

        Optional<TransactionLog> result = store.findByTransactionId(TRANSACTION_ID);

        assertTrue(result.isPresent());
        assertEquals(TRANSACTION_ID, result.orElseThrow().getTransactionId());
        assertEquals(17L, result.orElseThrow().getMovementId());
        assertEquals(new BigDecimal("3.2500"), result.orElseThrow().getQuantity());
        assertEquals(MovementType.ENTRADA, result.orElseThrow().getMovementType());
    }

    @Test
    void returnsEmptyWhenTransactionDoesNotExist() {
        when(valueOperations.get(TRANSACTION_KEY)).thenReturn(null);

        assertTrue(store.findByTransactionId(TRANSACTION_ID).isEmpty());
    }

    private TransactionLog transaction() {
        TransactionLog transaction = new TransactionLog();
        transaction.setTransactionId(TRANSACTION_ID);
        transaction.setMovementId(17L);
        transaction.setUserId(3L);
        transaction.setTransactionDate(LocalDateTime.of(2026, 10, 8, 10, 30));
        transaction.setSealId(22L);
        transaction.setProductId(41L);
        transaction.setReason("Recebimento de mercadoria");
        transaction.setMovementType(MovementType.ENTRADA);
        transaction.setQuantity(new BigDecimal("3.2500"));
        return transaction;
    }
}
