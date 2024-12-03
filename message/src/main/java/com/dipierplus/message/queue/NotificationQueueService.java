package com.dipierplus.message.queue;

import com.dipierplus.message.config.NotificationConfig;
import com.dipierplus.message.model.NotificationRequest;
import com.dipierplus.message.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class NotificationQueueService {
    private final RabbitTemplate rabbitTemplate;
    private final NotificationConfig config;

    private static final Map<NotificationType, String> ROUTING_KEYS = new HashMap<>() {{
        put(NotificationType.EMAIL, "email.notification");
        put(NotificationType.SMS, "sms.notification");
        put(NotificationType.PUSH, "push.notification");
    }};

    public void scheduleNotification(NotificationRequest request) {
        String routingKey = getRoutingKeyForType(request.getType());

        Integer delay;
        if (request.getScheduledAt() != null) {
            delay = calculateDelayInMillis(request.getScheduledAt());
        } else {
            delay = null;
        }

        rabbitTemplate.convertAndSend(
                config.getQueue().getExchangeName(),
                routingKey,
                request,
                message -> {
                    MessageProperties props = message.getMessageProperties();
                    if (delay != null && delay > 0) {
                        props.setHeader("x-delay", delay);
                    }
                    return message;
                }
        );

        log.info("Notification scheduled for routing key: {} with delay: {}", routingKey, delay);
    }

    @RabbitListener(queues = "${notification.queue.name}")
    public void processNotification(NotificationRequest request) {
        log.info("Processing notification: {}", request);
        // Aquí implementarías la lógica de procesamiento
        // Por ejemplo, llamar al NotificationService para enviar la notificación
    }

    private String getRoutingKeyForType(NotificationType type) {
        return ROUTING_KEYS.getOrDefault(type, "default.notification");
    }

    private Integer calculateDelayInMillis(LocalDateTime scheduledAt) {
        long now = System.currentTimeMillis();
        long scheduledTime = scheduledAt.toInstant(ZoneOffset.UTC).toEpochMilli();
        long delay = scheduledTime - now;

        return delay > 0 ? (int) delay : 0;
    }
}
