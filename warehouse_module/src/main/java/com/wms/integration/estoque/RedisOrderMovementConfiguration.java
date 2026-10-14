package com.wms.integration.estoque;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisOrderMovementConfiguration {
    @Bean
    public ChannelTopic orderMovementTopic(
            @Value("${warehouse.redis.orders.topic:warehouse:orders}") String topicName) {
        return new ChannelTopic(topicName);
    }

    @Bean
    public RedisMessageListenerContainer orderMovementListenerContainer(
            RedisConnectionFactory connectionFactory,
            OrderMovementMessageListener listener,
            ChannelTopic orderMovementTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listener, orderMovementTopic);
        return container;
    }
}
