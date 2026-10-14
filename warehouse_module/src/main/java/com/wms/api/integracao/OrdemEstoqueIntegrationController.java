package com.wms.api.integracao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.integration.estoque.OrderMovementPublisher;
import com.wms.integration.estoque.OrderMovementRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/integracao/ordens-estoque")
@Tag(name = "Integração de ordens de estoque", description = "Publicação de ordens externas no Redis")
public class OrdemEstoqueIntegrationController {
    private final ObjectMapper objectMapper;
    private final OrderMovementPublisher publisher;
    private final String topicName;

    public OrdemEstoqueIntegrationController(
            ObjectMapper objectMapper,
            OrderMovementPublisher publisher,
            @Value("${warehouse.redis.orders.topic:warehouse:orders}") String topicName) {
        this.objectMapper = objectMapper;
        this.publisher = publisher;
        this.topicName = topicName;
    }

    @PostMapping
    @Operation(summary = "Simular solicitação externa de entrada ou saída")
    public ResponseEntity<PublicationResponse> solicitarMovimentacao(
            @RequestBody OrderMovementRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A solicitação da ordem não foi informada");
        }
        try {
            request.validate();
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }

        String payload;
        try {
            payload = objectMapper.writeValueAsString(request);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Não foi possível serializar a solicitação de movimentação", exception);
        }

        Long subscribers = publisher.publish(payload);
        if (subscribers == null || subscribers == 0) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Nenhum consumidor está inscrito no tópico Redis");
        }

        return ResponseEntity.accepted().body(new PublicationResponse(request.code(), topicName, subscribers));
    }

    public record PublicationResponse(String orderCode, String topic, long subscribers) {
    }
}
