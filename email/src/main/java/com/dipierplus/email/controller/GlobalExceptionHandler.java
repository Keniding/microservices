package com.dipierplus.email.controller;

import com.dipierplus.email.dto.EmailResponse;
import com.dipierplus.email.exception.EmailServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailServiceException.class)
    public ResponseEntity<EmailResponse> handleEmailServiceException(EmailServiceException ex) {
        return ResponseEntity
                .status(ex.getStatus())
                .body(EmailResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EmailResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce("", (a, b) -> a + "; " + b);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EmailResponse.builder()
                        .success(false)
                        .message("Error de validación: " + errorMessage)
                        .build());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<EmailResponse> handleMaxSizeException(MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(EmailResponse.builder()
                        .success(false)
                        .message("El archivo adjunto excede el tamaño máximo permitido")
                        .build());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<EmailResponse> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(EmailResponse.builder()
                        .success(false)
                        .message("Error interno del servidor: " + ex.getMessage())
                        .build());
    }
}
