package com.wms.service.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.domain.identificacao.Selo;
import com.wms.repository.identificacao.SeloRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class SeloCacheService {
    private static final String KEY_PREFIX = "warehouse:cache:selo:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SeloRepository seloRepository;
    private final Duration cacheTtl;

    public SeloCacheService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            SeloRepository seloRepository,
            @Value("${warehouse.cache.selo.ttl:PT15M}") Duration cacheTtl) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.seloRepository = seloRepository;
        this.cacheTtl = cacheTtl;
    }

    public Optional<Selo> findByCode(String code) {
        String key = cacheKey(code);
        String cachedValue = redisTemplate.opsForValue().get(key);
        if (cachedValue != null) {
            return Optional.of(readValue(cachedValue));
        }

        return seloRepository.findByCode(code).map(selo -> {
            redisTemplate.opsForValue().set(key, writeValue(selo), cacheTtl);
            return selo;
        });
    }

    public void cache(Selo selo) {
        if (selo == null || selo.getCode() == null || selo.getCode().isBlank()) {
            return;
        }
        redisTemplate.opsForValue().set(cacheKey(selo.getCode()), writeValue(selo), cacheTtl);
    }

    public void invalidate(String code) {
        redisTemplate.delete(cacheKey(code));
    }

    private String cacheKey(String code) {
        return KEY_PREFIX + code;
    }

    private String writeValue(Selo selo) {
        try {
            return objectMapper.writeValueAsString(selo);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível serializar o selo em cache", exception);
        }
    }

    private Selo readValue(String value) {
        try {
            return objectMapper.readValue(value, Selo.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível desserializar o selo em cache", exception);
        }
    }
}
