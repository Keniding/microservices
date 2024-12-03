package com.dipierplus.message.model;

import com.dipierplus.message.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class Template {
    private String id;
    private String name;
    private String content;
    private NotificationType type;
    private Map<String, String> parameters;
}
