package com.dipierplus.email.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class EmailServiceException extends RuntimeException {
    private final HttpStatus status;

    public EmailServiceException(String message) {
        super(message);
        this.status = HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public EmailServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}

