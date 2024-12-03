package com.dipierplus.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailRequest {
    @NotBlank(message = "El email no puede estar vacío")
    @Email(message = "El formato del email no es válido",
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")
    private String to;

    @NotBlank(message = "El asunto no puede estar vacío")
    @Size(max = 100, message = "El asunto no puede exceder los 100 caracteres")
    private String subject;

    @NotBlank(message = "El contenido no puede estar vacío")
    private String body;

    @Size(max = 100, message = "El nombre del destinatario no puede exceder los 100 caracteres")
    private String recipientName;

    @Size(max = 50, message = "El texto del botón no puede exceder los 50 caracteres")
    private String buttonText;

    private String buttonUrl;

    @Size(max = 200, message = "El texto del pie no puede exceder los 200 caracteres")
    private String footerText;
}
