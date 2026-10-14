package com.wms.integration.estoque;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.service.estoque.OrderMovementProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class OrderMovementMessageListener implements MessageListener {
    private static final Logger logger = LoggerFactory.getLogger(OrderMovementMessageListener.class);

    private final ObjectMapper objectMapper;
    private final OrderMovementProcessor processor;

    public OrderMovementMessageListener(ObjectMapper objectMapper, OrderMovementProcessor processor) {
        this.objectMapper = objectMapper;
        this.processor = processor;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            OrderMovementRequest request = objectMapper.readValue(payload, OrderMovementRequest.class);
            processor.process(request);
        } catch (JsonProcessingException | RuntimeException exception) {
            logger.error("Não foi possível processar a solicitação de movimentação recebida do Redis", exception);
        }
    }
}
