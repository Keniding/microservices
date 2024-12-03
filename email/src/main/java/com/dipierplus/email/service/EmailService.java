package com.dipierplus.email.service;

import com.dipierplus.email.dto.EmailRequest;
import org.springframework.web.multipart.MultipartFile;

public interface EmailService {
    void sendEmail(EmailRequest emailRequest);
    void sendEmailWithAttachment(EmailRequest emailRequest, MultipartFile attachment);
    void sendEmailWithTemplate(EmailRequest emailRequest, String templateName);
}
