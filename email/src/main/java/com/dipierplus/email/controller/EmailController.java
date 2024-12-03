package com.dipierplus.email.controller;

import com.dipierplus.email.dto.EmailRequest;
import com.dipierplus.email.dto.EmailResponse;
import com.dipierplus.email.exception.EmailServiceException;
import com.dipierplus.email.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<EmailResponse> sendEmail(@Valid @RequestBody EmailRequest emailRequest) {
        try {
            emailService.sendEmail(emailRequest);
            return ResponseEntity.ok(EmailResponse.builder()
                    .success(true)
                    .message("Email enviado exitosamente")
                    .recipient(emailRequest.getTo())
                    .build());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailResponse.builder()
                            .success(false)
                            .message("Error al enviar el email: " + e.getMessage())
                            .recipient(emailRequest.getTo())
                            .build());
        }
    }

    @PostMapping(value = "/send-with-attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EmailResponse> sendEmailWithAttachment(
            @RequestPart("emailRequest") @Valid EmailRequest emailRequest,
            @RequestPart(value = "file", required = true) MultipartFile file) {
        try {
            emailService.sendEmailWithAttachment(emailRequest, file);
            return ResponseEntity.ok(EmailResponse.builder()
                    .success(true)
                    .message("Email con archivo adjunto enviado exitosamente")
                    .recipient(emailRequest.getTo())
                    .attachmentName(file.getOriginalFilename())
                    .build());
        } catch (EmailServiceException e) {
            return ResponseEntity
                    .status(e.getStatus())
                    .body(EmailResponse.builder()
                            .success(false)
                            .message(e.getMessage())
                            .recipient(emailRequest.getTo())
                            .build());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailResponse.builder()
                            .success(false)
                            .message("Error interno del servidor: " + e.getMessage())
                            .recipient(emailRequest.getTo())
                            .build());
        }
    }

    @PostMapping("/send-template/{templateName}")
    public ResponseEntity<EmailResponse> sendEmailTemplate(
            @PathVariable String templateName,
            @Valid @RequestBody EmailRequest emailRequest) {
        try {
            emailService.sendEmailWithTemplate(emailRequest, templateName);
            return ResponseEntity.ok(EmailResponse.builder()
                    .success(true)
                    .message("Email con plantilla enviado exitosamente")
                    .recipient(emailRequest.getTo())
                    .templateUsed(templateName)
                    .build());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(EmailResponse.builder()
                            .success(false)
                            .message("Error al enviar el email: " + e.getMessage())
                            .recipient(emailRequest.getTo())
                            .build());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Email Service");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
