package com.dipierplus.message.model;

import com.dipierplus.message.enums.NotificationPriority;
import com.dipierplus.message.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class NotificationRequest {
    private String id;
    private NotificationType type;
    private NotificationPriority priority;
    private String recipient;
    private Map<String, Object> content;
    private Map<String, Object> metadata;
    private LocalDateTime scheduledAt;
}
