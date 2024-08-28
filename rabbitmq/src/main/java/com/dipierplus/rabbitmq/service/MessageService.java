package com.dipierplus.rabbitmq.service;

import com.dipierplus.rabbitmq.configuration.RabbitMQConfig;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessageService {

    private final RabbitTemplate rabbitTemplate;

    public void sendMessage(String routingKey, Object message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, routingKey, message);
    }
}
