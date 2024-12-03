package com.dipierplus.message.provider;

import com.dipierplus.message.enums.NotificationType;
import com.dipierplus.message.model.NotificationRequest;
import com.dipierplus.message.model.NotificationResponse;

public interface NotificationProvider {
    NotificationType getType();
    NotificationResponse send(NotificationRequest request);
    boolean isAvailable();
}
