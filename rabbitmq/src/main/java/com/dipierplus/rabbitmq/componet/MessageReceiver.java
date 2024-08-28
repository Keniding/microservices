package com.dipierplus.rabbitmq.componet;

import com.dipierplus.rabbitmq.configuration.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import com.rabbitmq.client.Channel;

@Component
public class MessageReceiver {

    private static final Logger logger = LoggerFactory.getLogger(MessageReceiver.class);

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME, containerFactory = "rabbitListenerContainerFactory")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            logger.info("Received message: {}", message);
            processMessage(message);
            channel.basicAck(tag, false);
            logger.info("Message processed and acknowledged successfully.");
        } catch (Exception e) {

            logger.error("Error processing message: {}", message, e);

            try {
                channel.basicNack(tag, false, true);
                logger.info("Message requeued due to processing error.");
            } catch (Exception ex) {
                logger.error("Error acknowledging message", ex);
            }
        }
    }

    private void processMessage(String message) {
        logger.debug("Processing message: {}", message);
        if (message.contains("error")) {
            throw new RuntimeException("Simulated processing error");
        }
    }
}