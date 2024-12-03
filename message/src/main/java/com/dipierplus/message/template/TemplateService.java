package com.dipierplus.message.template;

import com.dipierplus.message.exception.TemplateNotFoundException;
import com.dipierplus.message.model.NotificationRequest;
import com.dipierplus.message.model.Template;
import com.dipierplus.message.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TemplateService {
    private final TemplateRepository templateRepository;
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{\\{(.*?)}}");

    public NotificationRequest processTemplate(NotificationRequest request) {
        // Obtener el ID de la plantilla del contenido de la solicitud
        String templateId = (String) request.getContent().get("templateId");
        if (templateId == null) {
            throw new IllegalArgumentException("Template ID is required");
        }

        // Buscar la plantilla en la base de datos
        Template template = templateRepository.findById(templateId)
                .orElseThrow(() -> new TemplateNotFoundException("Template not found with ID: " + templateId));

        // Obtener los parámetros para la plantilla
        @SuppressWarnings("unchecked")
        Map<String, Object> parameters = (Map<String, Object>) request.getContent()
                .getOrDefault("parameters", Map.of());

        // Procesar la plantilla con los parámetros
        String processedContent = processTemplateContent(template.getContent(), parameters);

        // Actualizar el contenido de la solicitud con el contenido procesado
        request.getContent().put("body", processedContent);

        // Si la plantilla tiene un asunto predefinido y no se ha especificado uno
        if (template.getParameters().containsKey("subject") && !request.getContent().containsKey("subject")) {
            request.getContent().put("subject", template.getParameters().get("subject"));
        }

        return request;
    }

    private String processTemplateContent(String template, Map<String, Object> parameters) {
        if (template == null || parameters == null) {
            return template;
        }

        StringBuilder result = new StringBuilder();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);

        while (matcher.find()) {
            String placeholder = matcher.group(1).trim();
            Object value = parameters.getOrDefault(placeholder, "");
            matcher.appendReplacement(result, Matcher.quoteReplacement(value.toString()));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    public Template createTemplate(Template template) {
        return templateRepository.save(template);
    }

    public Template updateTemplate(String id, Template template) {
        if (!templateRepository.existsById(id)) {
            throw new TemplateNotFoundException("Template not found with ID: " + id);
        }
        template.setId(id);
        return templateRepository.save(template);
    }

    public void deleteTemplate(String id) {
        if (!templateRepository.existsById(id)) {
            throw new TemplateNotFoundException("Template not found with ID: " + id);
        }
        templateRepository.deleteById(id);
    }
}
