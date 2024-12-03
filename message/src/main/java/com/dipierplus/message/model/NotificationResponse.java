package com.dipierplus.message.model;

import com.dipierplus.message.enums.NotificationPriority;
import com.dipierplus.message.enums.NotificationStatus;
import com.dipierplus.message.enums.NotificationType;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class NotificationResponse {
    private String id;
    private NotificationStatus status;
    private String message;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
}

