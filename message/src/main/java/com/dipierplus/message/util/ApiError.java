package com.dipierplus.message.util;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;

@lombok.Data
@AllArgsConstructor
public class ApiError {
    private String message;
    private HttpStatus status;
    private String timestamp;
}
