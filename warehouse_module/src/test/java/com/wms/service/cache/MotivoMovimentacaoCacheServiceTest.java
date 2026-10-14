package com.wms.service.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.domain.operacoes.ajustes.MotivoMovimentacao;
import com.wms.repository.operacoes.MotivoMovimentacaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MotivoMovimentacaoCacheServiceTest {
    private static final String CODE = "DANIFICADO";
    private static final String CACHE_KEY = "warehouse:cache:motivo-movimentacao:" + CODE;
    private static final Duration TTL = Duration.ofMinutes(30);

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private MotivoMovimentacaoRepository repository;

    private ObjectMapper objectMapper;
    private MotivoMovimentacaoCacheService service;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        service = new MotivoMovimentacaoCacheService(redisTemplate, objectMapper, repository, TTL);
    }

    @Test
    void returnsCachedValueWithoutQueryingRepository() throws Exception {
        MotivoMovimentacao motivo = motivo();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn(objectMapper.writeValueAsString(motivo));

        Optional<MotivoMovimentacao> result = service.findByCode(CODE);

        assertTrue(result.isPresent());
        assertEquals(CODE, result.orElseThrow().getCode());
        assertEquals("Produto danificado", result.orElseThrow().getDescription());
        verifyNoInteractions(repository);
    }

    @Test
    void loadsFromRepositoryAndCachesOnMiss() {
        MotivoMovimentacao motivo = motivo();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn(null);
        when(repository.findByCode(CODE)).thenReturn(Optional.of(motivo));

        Optional<MotivoMovimentacao> result = service.findByCode(CODE);

        assertEquals(Optional.of(motivo), result);
        verify(valueOperations).set(CACHE_KEY, write(motivo), TTL);
    }

    @Test
    void returnsEmptyWhenRepositoryHasNoMatchingReason() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn(null);
        when(repository.findByCode(CODE)).thenReturn(Optional.empty());

        assertTrue(service.findByCode(CODE).isEmpty());
        verify(valueOperations, never()).set(anyString(), anyString(), any(Duration.class));
    }

    @Test
    void cachesProvidedReasonWithConfiguredTtl() {
        MotivoMovimentacao motivo = motivo();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        service.cache(motivo);

        verify(valueOperations).set(CACHE_KEY, write(motivo), TTL);
    }

    @Test
    void invalidatesReasonKey() {
        service.invalidate(CODE);

        verify(redisTemplate).delete(CACHE_KEY);
    }

    @Test
    void reportsCorruptedCachedJsonInsteadOfSilentlyFallingBack() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(CACHE_KEY)).thenReturn("{invalid-json");

        assertThrows(IllegalStateException.class, () -> service.findByCode(CODE));
        verifyNoInteractions(repository);
    }

    private MotivoMovimentacao motivo() {
        MotivoMovimentacao motivo = new MotivoMovimentacao();
        motivo.setCode(CODE);
        motivo.setDescription("Produto danificado");
        motivo.setActive(true);
        return motivo;
    }

    private String write(MotivoMovimentacao motivo) {
        try {
            return objectMapper.writeValueAsString(motivo);
        } catch (Exception exception) {
            throw new AssertionError(exception);
        }
    }
}
