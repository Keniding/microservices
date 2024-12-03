package com.dipierplus.email.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EmailResponse {
    private boolean success;
    private String message;
    private String recipient;
    private String attachmentName;
    private String templateUsed;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
