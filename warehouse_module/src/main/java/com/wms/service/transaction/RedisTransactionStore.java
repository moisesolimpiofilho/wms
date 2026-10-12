package com.wms.service.transaction;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.domain.auditoria.TransactionLog;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RedisTransactionStore {
    private static final String KEY_PREFIX = "warehouse:transactions:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisTransactionStore(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void save(TransactionLog transaction) {
        String key = transactionKey(transaction.getTransactionId());
        String serializedTransaction = writeValue(transaction);
        Boolean stored = redisTemplate.opsForValue().setIfAbsent(key, serializedTransaction);
        if (Boolean.FALSE.equals(stored)) {
            throw new IllegalStateException(
                    "Já existe uma transação armazenada com o identificador " + transaction.getTransactionId());
        }
        if (stored == null) {
            throw new IllegalStateException("Redis não confirmou o armazenamento da transação " + transaction.getTransactionId());
        }
    }

    public Optional<TransactionLog> findByTransactionId(String transactionId) {
        String serializedTransaction = redisTemplate.opsForValue().get(transactionKey(transactionId));
        if (serializedTransaction == null) {
            return Optional.empty();
        }
        return Optional.of(readValue(serializedTransaction));
    }

    private String transactionKey(String transactionId) {
        return KEY_PREFIX + transactionId;
    }

    private String writeValue(TransactionLog transaction) {
        try {
            return objectMapper.writeValueAsString(transaction);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível serializar a transação", exception);
        }
    }

    private TransactionLog readValue(String value) {
        try {
            return objectMapper.readValue(value, TransactionLog.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível desserializar a transação armazenada", exception);
        }
    }
}
