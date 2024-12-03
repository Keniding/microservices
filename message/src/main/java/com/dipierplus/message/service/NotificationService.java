package com.dipierplus.message.service;

import com.dipierplus.message.enums.NotificationStatus;
import com.dipierplus.message.enums.NotificationType;
import com.dipierplus.message.model.NotificationRequest;
import com.dipierplus.message.model.NotificationResponse;
import com.dipierplus.message.provider.NotificationProvider;
import com.dipierplus.message.queue.NotificationQueueService;
import com.dipierplus.message.template.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.ProviderNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NotificationService {
    private final Map<NotificationType, NotificationProvider> providers;
    private final NotificationQueueService queueService;
    private final TemplateService templateService;

    public NotificationService(List<NotificationProvider> providerList,
                               NotificationQueueService queueService,
                               TemplateService templateService) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(NotificationProvider::getType, Function.identity()));
        this.queueService = queueService;
        this.templateService = templateService;
    }

    public NotificationResponse sendNotification(NotificationRequest request) {
        validateRequest(request);

        if (request.getScheduledAt() != null &&
                request.getScheduledAt().isAfter(LocalDateTime.now())) {
            return scheduleNotification(request);
        }

        NotificationProvider provider = providers.get(request.getType());
        if (provider == null || !provider.isAvailable()) {
            throw new ProviderNotFoundException("No available provider for type: " + request.getType());
        }

        // Process template if needed
        if (request.getContent().containsKey("templateId")) {
            request = templateService.processTemplate(request);
        }

        try {
            NotificationResponse response = provider.send(request);
            logNotification(request, response);
            return response;
        } catch (Exception e) {
            handleFailure(request, e);
            throw e;
        }
    }

    private NotificationResponse scheduleNotification(NotificationRequest request) {
        queueService.scheduleNotification(request);
        return NotificationResponse.builder()
                .id(request.getId())
                .status(NotificationStatus.PENDING)
                .message("Notification scheduled")
                .build();
    }

    private void validateRequest(NotificationRequest request) {
        // Implementar validaciones
    }

    private void logNotification(NotificationRequest request, NotificationResponse response) {
        // Implementar logging
    }

    private void handleFailure(NotificationRequest request, Exception e) {
        // Implementar manejo de fallos
    }
}
