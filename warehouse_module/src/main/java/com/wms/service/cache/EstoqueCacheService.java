package com.wms.service.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.domain.estoque.Estoque;
import com.wms.repository.estoque.EstoqueRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class EstoqueCacheService {
    private static final String KEY_PREFIX = "warehouse:cache:estoque:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final EstoqueRepository estoqueRepository;
    private final Duration cacheTtl;

    public EstoqueCacheService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            EstoqueRepository estoqueRepository,
            @Value("${warehouse.cache.estoque.ttl:PT15M}") Duration cacheTtl) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.estoqueRepository = estoqueRepository;
        this.cacheTtl = cacheTtl;
    }

    public Optional<Estoque> findByKey(Long productId, Long batchId, Long locationId, Long logisticUnitId) {
        String key = cacheKey(productId, batchId, locationId, logisticUnitId);
        String cachedValue = redisTemplate.opsForValue().get(key);
        if (cachedValue != null) {
            return Optional.of(readValue(cachedValue));
        }

        return estoqueRepository.lockActiveByProductAndLocation(productId, batchId, locationId, logisticUnitId)
                .stream()
                .findFirst()
                .map(estoque -> {
                    redisTemplate.opsForValue().set(key, writeValue(estoque), cacheTtl);
                    return estoque;
                });
    }

    public void cache(Estoque estoque) {
        if (estoque == null || estoque.getProductId() == null || estoque.getLocationId() == null) {
            return;
        }
        redisTemplate.opsForValue().set(
                cacheKey(estoque.getProductId(), estoque.getBatchId(), estoque.getLocationId(), estoque.getLogisticUnitId()),
                writeValue(estoque),
                cacheTtl);
    }

    public void invalidate(Long productId, Long batchId, Long locationId, Long logisticUnitId) {
        redisTemplate.delete(cacheKey(productId, batchId, locationId, logisticUnitId));
    }

    private String cacheKey(Long productId, Long batchId, Long locationId, Long logisticUnitId) {
        return KEY_PREFIX + productId + ":" + batchId + ":" + locationId + ":" + logisticUnitId;
    }

    private String writeValue(Estoque estoque) {
        try {
            return objectMapper.writeValueAsString(estoque);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível serializar o estoque em cache", exception);
        }
    }

    private Estoque readValue(String value) {
        try {
            return objectMapper.readValue(value, Estoque.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível desserializar o estoque em cache", exception);
        }
    }
}
