package com.dipierplus.email.imp;

import com.dipierplus.email.dto.EmailRequest;
import com.dipierplus.email.exception.EmailServiceException;
import com.dipierplus.email.service.EmailService;
import com.dipierplus.email.utils.EmailValidator;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendEmail(EmailRequest emailRequest) {
        validateEmailRequest(emailRequest);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            log.debug("Configurando email - From: {}, To: {}", fromEmail, emailRequest.getTo());
            helper.setFrom(fromEmail);
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());
            helper.setText(emailRequest.getBody(), true);

            log.debug("Enviando email...");
            mailSender.send(message);
            log.info("Email enviado exitosamente a: {}", emailRequest.getTo());

        } catch (MessagingException e) {
            log.error("Error al enviar email - Mensaje: {} - Causa: {}", e.getMessage(), e.getCause() != null ? e.getCause().getMessage() : "No hay causa", e);
            throw new EmailServiceException("Error al enviar el email: " + e.getMessage());
        }
    }

    private void validateEmailRequest(EmailRequest emailRequest) {
        if (emailRequest == null) {
            throw new EmailServiceException("La solicitud de email no puede ser nula");
        }

        String to = emailRequest.getTo();
        if (to == null || to.trim().isEmpty()) {
            throw new EmailServiceException("La dirección de destino no puede estar vacía");
        }

        if (!EmailValidator.isValidEmailAddress(to)) {
            throw new EmailServiceException("Dirección de email inválida: " + to);
        }

        String subject = emailRequest.getSubject();
        if (subject == null || subject.trim().isEmpty()) {
            throw new EmailServiceException("El asunto no puede estar vacío");
        }

        String body = emailRequest.getBody();
        if (body == null || body.trim().isEmpty()) {
            throw new EmailServiceException("El contenido no puede estar vacío");
        }
    }

    private final List<String> supportedContentTypes = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "image/jpeg",
            "image/png",
            "image/gif",
            "text/plain"
    );

    @Override
    public void sendEmailWithAttachment(EmailRequest emailRequest, MultipartFile attachment) {
        try {
            String contentType = attachment.getContentType();
            if (contentType == null || !supportedContentTypes.contains(contentType.toLowerCase())) {
                throw new EmailServiceException(
                        "Tipo de archivo no soportado: " + contentType,
                        HttpStatus.BAD_REQUEST
                );
            }

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());
            helper.setText(emailRequest.getBody(), true);

            if (!attachment.isEmpty()) {
                String fileName = attachment.getOriginalFilename();
                if (fileName == null || fileName.trim().isEmpty()) {
                    fileName = "attachment" + getFileExtension(contentType);
                }

                helper.addAttachment(fileName, attachment);
            }

            mailSender.send(message);
            log.info("Email con archivo adjunto enviado exitosamente a: {}", emailRequest.getTo());

        } catch (MessagingException e) {
            log.error("Error al enviar el email con archivo adjunto: {}", e.getMessage());
            throw new EmailServiceException("Error al enviar el email con archivo adjunto: " + e.getMessage());
        }
    }

    private String getFileExtension(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "application/pdf" -> ".pdf";
            case "application/msword" -> ".doc";
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document" -> ".docx";
            case "application/vnd.ms-excel" -> ".xls";
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet" -> ".xlsx";
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "text/plain" -> ".txt";
            default -> "";
        };
    }
    public void sendEmailWithTemplate(EmailRequest emailRequest, String templateName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(emailRequest.getTo());
            helper.setSubject(emailRequest.getSubject());

            String htmlContent = getTemplateByName(templateName, emailRequest);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email con plantilla '{}' enviado exitosamente a: {}", templateName, emailRequest.getTo());

        } catch (MessagingException e) {
            log.error("Error al enviar el email con plantilla '{}': {}", templateName, e.getMessage());
            throw new EmailServiceException("Error al enviar el email con plantilla: " + e.getMessage());
        }
    }

    private String getTemplateByName(String templateName, EmailRequest emailRequest) {
        return switch (templateName.toLowerCase()) {
            case "welcome" -> getWelcomeTemplate(emailRequest);
            case "notification" -> getNotificationTemplate(emailRequest);
            case "reset-password" -> getResetPasswordTemplate(emailRequest);
            case "confirmation" -> getConfirmationTemplate(emailRequest);
            default -> getDefaultTemplate(emailRequest);
        };
    }

    private String getWelcomeTemplate(EmailRequest emailRequest) {
        return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                /* Estilos base */
                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }
                .content { padding: 20px; background-color: #fff; }
                .button { background-color: #4CAF50; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="header">
                    <h1>¡Bienvenido a nuestro servicio!</h1>
                </div>
                <div class="content">
                    <h2>Hola %s</h2>
                    <p>%s</p>
                    %s
                    <p>Saludos cordiales,<br>El equipo de soporte</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                emailRequest.getRecipientName(),
                emailRequest.getBody(),
                generateButtonHtml(emailRequest)
        );
    }

    private String getNotificationTemplate(EmailRequest emailRequest) {
        return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .notification { background-color: #f8f9fa; padding: 20px; border-left: 4px solid #007bff; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="notification">
                    <h2>%s</h2>
                    <p>%s</p>
                    %s
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                emailRequest.getSubject(),
                emailRequest.getBody(),
                generateButtonHtml(emailRequest)
        );
    }

    private String getResetPasswordTemplate(EmailRequest emailRequest) {
        return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .warning { color: #721c24; background-color: #f8d7da; padding: 20px; margin: 20px 0; }
            </style>
        </head>
        <body>
            <div class="container">
                <h2>Recuperación de Contraseña</h2>
                <p>Hola %s,</p>
                <p>%s</p>
                %s
                <div class="warning">
                    <p>Este enlace expirará en 24 horas por seguridad.</p>
                </div>
            </div>
        </body>
        </html>
        """.formatted(
                emailRequest.getRecipientName(),
                emailRequest.getBody(),
                generateButtonHtml(emailRequest)
        );
    }

    private String getConfirmationTemplate(EmailRequest emailRequest) {
        return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                .success { color: #155724; background-color: #d4edda; padding: 20px; margin: 20px 0; }
            </style>
        </head>
        <body>
            <div class="container">
                <div class="success">
                    <h2>Confirmación</h2>
                    <p>%s</p>
                </div>
                %s
            </div>
        </body>
        </html>
        """.formatted(
                emailRequest.getBody(),
                generateButtonHtml(emailRequest)
        );
    }

    private String getDefaultTemplate(EmailRequest emailRequest) {
        return """
        <!DOCTYPE html>
        <html lang="es">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body { font-family: Arial, sans-serif; line-height: 1.6; }
                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
            </style>
        </head>
        <body>
            <div class="container">
                <h2>%s</h2>
                <p>%s</p>
                %s
            </div>
        </body>
        </html>
        """.formatted(
                emailRequest.getSubject(),
                emailRequest.getBody(),
                generateButtonHtml(emailRequest)
        );
    }

    private String generateButtonHtml(EmailRequest emailRequest) {
        if (emailRequest.getButtonUrl() != null && emailRequest.getButtonText() != null) {
            return String.format(
                    "<div style='text-align: center; margin: 25px 0;'>" +
                            "<a href='%s' style='display: inline-block; padding: 12px 24px; " +
                            "background-color: #007bff; color: #ffffff; text-decoration: none; " +
                            "border-radius: 5px; font-weight: bold;'>%s</a></div>",
                    emailRequest.getButtonUrl().trim(),
                    emailRequest.getButtonText().trim()
            );
        }
        return "";
    }
}
