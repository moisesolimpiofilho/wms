package com.wms.integration.estoque;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Service
public class OrderMovementPublisher {
    private final StringRedisTemplate redisTemplate;
    private final ChannelTopic orderMovementTopic;

    public OrderMovementPublisher(StringRedisTemplate redisTemplate, ChannelTopic orderMovementTopic) {
        this.redisTemplate = redisTemplate;
        this.orderMovementTopic = orderMovementTopic;
    }

    public Long publish(String serializedRequest) {
        return redisTemplate.convertAndSend(orderMovementTopic.getTopic(), serializedRequest);
    }
}
