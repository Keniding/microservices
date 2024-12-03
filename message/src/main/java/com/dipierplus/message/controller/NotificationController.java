package com.dipierplus.message.controller;

import com.dipierplus.message.exception.NotificationException;
import com.dipierplus.message.model.NotificationRequest;
import com.dipierplus.message.model.NotificationResponse;
import com.dipierplus.message.service.NotificationService;
import com.dipierplus.message.util.ApiError;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated
@Slf4j
@AllArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(
            @Valid @RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NotificationResponse> getStatus(@PathVariable String id) {
        // Implementar
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelNotification(@PathVariable String id) {
        // Implementar
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NotificationException.class)
    public ResponseEntity<ApiError> handleNotificationException(NotificationException e) {
        ApiError error = new ApiError(
                e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                java.time.LocalDateTime.now().toString()
        );
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}