package com.wms.service.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.domain.operacoes.ajustes.MotivoMovimentacao;
import com.wms.repository.operacoes.MotivoMovimentacaoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class MotivoMovimentacaoCacheService {
    private static final String KEY_PREFIX = "warehouse:cache:motivo-movimentacao:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final MotivoMovimentacaoRepository repository;
    private final Duration cacheTtl;

    public MotivoMovimentacaoCacheService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            MotivoMovimentacaoRepository repository,
            @Value("${warehouse.cache.motivo-movimentacao.ttl:PT30M}") Duration cacheTtl) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.repository = repository;
        this.cacheTtl = cacheTtl;
    }

    public Optional<MotivoMovimentacao> findByCode(String code) {
        String key = cacheKey(code);
        String cachedValue = redisTemplate.opsForValue().get(key);
        if (cachedValue != null) {
            return Optional.of(readValue(cachedValue));
        }

        return repository.findByCode(code).map(motivo -> {
            redisTemplate.opsForValue().set(key, writeValue(motivo), cacheTtl);
            return motivo;
        });
    }

    public void cache(MotivoMovimentacao motivo) {
        redisTemplate.opsForValue().set(
                cacheKey(motivo.getCode()),
                writeValue(motivo),
                cacheTtl);
    }

    public void invalidate(String code) {
        redisTemplate.delete(cacheKey(code));
    }

    private String cacheKey(String code) {
        return KEY_PREFIX + code;
    }

    private String writeValue(MotivoMovimentacao motivo) {
        try {
            return objectMapper.writeValueAsString(motivo);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível serializar o motivo de movimentação", exception);
        }
    }

    private MotivoMovimentacao readValue(String value) {
        try {
            return objectMapper.readValue(value, MotivoMovimentacao.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível desserializar o motivo de movimentação em cache", exception);
        }
    }
}
