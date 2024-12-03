package com.dipierplus.message.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "notification")
@Data
public class NotificationConfig {
    private EmailConfig email;
    private SmsConfig sms;
    private PushConfig push;
    private QueueConfig queue;

    @Data
    public static class EmailConfig {
        private String host;
        private int port;
        private String username;
        private String password;
        private boolean ssl;
        private int maxRetries;
    }

    @Data
    public static class SmsConfig {
        private String twilioAccountSid;
        private String twilioAuthToken;
        private String fromNumber;
        private int maxRetries;
    }

    @Data
    public static class PushConfig {
        private String firebaseConfigPath;
        private int maxRetries;
    }

    @Data
    public static class QueueConfig {
        private String host;
        private int port;
        private String username;
        private String password;
        private String exchangeName;
    }
}
