package com.dipierplus.downloads.service;

import com.dipierplus.downloads.model.*;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.ConverterProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGeneratorService {
    private final TemplateEngine templateEngine;
    private final BillingServiceImp invoiceService;
    private final ProductService productService;
    private final PaymentHistoryServiceImp paymentHistoryService;
    private final CartServiceImpl cartServiceImpl;

    public ResponseEntity<Resource> generateInvoicePdf(String invoiceId) {
        try {
            // 1. Validar y obtener la factura
            Invoice invoice;
            try {
                invoice = invoiceService.getInvoice(invoiceId);
                if (invoice == null) {
                    log.error("Factura no encontrada: {}", invoiceId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new ByteArrayResource(
                                    createErrorResponse("Factura no encontrada", "INVOICE_NOT_FOUND").getBytes()
                            ));
                }
                log.info("Factura encontrada: {}", invoice);
            } catch (Exception e) {
                log.error("Error al obtener la factura {}: {}", invoiceId, e.getMessage());
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ByteArrayResource(
                                createErrorResponse("Error al obtener la factura", "INVOICE_ERROR").getBytes()
                        ));
            }

            // 2. Obtener el historial de pago
            PaymentHistory paymentHistory;
            try {
                paymentHistory = paymentHistoryService.getInvoicePaymentHistory(invoiceId);
                if (paymentHistory == null) {
                    log.error("Historial de pago no encontrado para la factura: {}", invoiceId);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(new ByteArrayResource(
                                    createErrorResponse("Historial de pago no encontrado", "PAYMENT_HISTORY_NOT_FOUND").getBytes()
                            ));
                }
                log.info("Historial de pago obtenido para la factura: {}", paymentHistory);
            } catch (Exception e) {
                log.error("Error al obtener el historial de pago para la factura {}: {}", invoiceId, e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ByteArrayResource(
                                createErrorResponse("Error al obtener el historial de pago", "PAYMENT_HISTORY_ERROR").getBytes()
                        ));
            }

            // 3. Construir el DTO con la información
            InvoiceDetailsDTO invoiceDetails = new InvoiceDetailsDTO();
            invoiceDetails.setInvoice(invoice);
            invoiceDetails.setPaymentHistory(paymentHistory);

            // 4. Obtener detalles de productos
            List<ProductDetailsDTO> productDetails = getProductDetails(invoice);
            if (productDetails.isEmpty()) {
                log.warn("No se encontraron productos para la factura: {}", invoiceId);
            }
            invoiceDetails.setProducts(productDetails);

            // 5. Calcular el total
            BigDecimal total = calculateTotal(productDetails);

            // 6. Preparar el contexto para la plantilla
            Context context = new Context();
            context.setVariable("invoiceDetails", invoiceDetails);
            context.setVariable("dateFormatter", DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            context.setVariable("total", total);

            // 7. Procesar la plantilla
            String html = templateEngine.process("invoice-template", context);

            // 8. Configurar propiedades del convertidor
            ConverterProperties properties = new ConverterProperties();
            properties.setBaseUri("classpath:/templates/");

            // 9. Generar el PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            HtmlConverter.convertToPdf(html, outputStream, properties);

            // 10. Preparar el recurso para la respuesta
            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

            // 11. Generar nombre del archivo
            String fileName = String.format("factura-%s-%s.pdf",
                    invoice.getId(),
                    DateTimeFormatter.ofPattern("yyyyMMdd").format(invoice.getPaymentDate()));

            // 12. Construir y retornar la respuesta
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (Exception e) {
            log.error("Error inesperado al generar el PDF para la factura {}: {}", invoiceId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ByteArrayResource(
                            createErrorResponse("Error interno del servidor", "INTERNAL_SERVER_ERROR").getBytes()
                    ));
        }
    }

    private List<ProductDetailsDTO> getProductDetails(Invoice invoice) {
        List<ProductDetailsDTO> products = new ArrayList<>();

        try {
            Cart cart = cartServiceImpl.getCart(invoice.getCustomerId());
            if (cart == null || cart.getItems() == null) {
                log.warn("Carrito no encontrado para el cliente: {}", invoice.getCustomerId());
                return products;
            }

            for (CartItem cartItem : cart.getItems()) {
                productService.getProductById(cartItem.getProductId())
                        .ifPresent(product -> {
                            ProductDetailsDTO productDetails = new ProductDetailsDTO();
                            productDetails.setId(product.getId());
                            productDetails.setName(product.getName());
                            productDetails.setDescription(product.getDescription());
                            productDetails.setPrice(product.getPrice());
                            productDetails.setQuantity(cartItem.getQuantity());
                            productDetails.setSubtotal(
                                    product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                            );
                            products.add(productDetails);
                        });
            }
        } catch (Exception e) {
            log.error("Error al obtener detalles de productos: {}", e.getMessage());
        }

        return products;
    }

    private BigDecimal calculateTotal(List<ProductDetailsDTO> products) {
        return products.stream()
                .map(ProductDetailsDTO::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public ResponseEntity<Resource> generateQuotePdf(Map<String, Object> quoteData) {
        try {
            // 1. Validar datos de cotización
            if (quoteData == null || !quoteData.containsKey("quoteNumber")) {
                return ResponseEntity.badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ByteArrayResource(
                                createErrorResponse("Datos de cotización inválidos", "INVALID_QUOTE_DATA").getBytes()
                        ));
            }

            // 2. Preparar el contexto
            Context context = new Context();
            context.setVariable("quoteData", quoteData);
            context.setVariable("currentDate", LocalDateTime.now());

            // 3. Procesar la plantilla
            String processedHtml = templateEngine.process("quote-template", context);

            // 4. Generar PDF
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(processedHtml);
            renderer.layout();
            renderer.createPDF(outputStream, false);
            renderer.finishPDF();

            // 5. Preparar el recurso
            ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

            // 6. Generar nombre del archivo
            String fileName = String.format("cotizacion-%s-%s.pdf",
                    quoteData.get("quoteNumber"),
                    DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now()));

            // 7. Construir y retornar la respuesta
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(resource.contentLength())
                    .body(resource);

        } catch (Exception e) {
            log.error("Error al generar PDF de cotización: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ByteArrayResource(
                            createErrorResponse("Error al generar la cotización", "QUOTE_GENERATION_ERROR").getBytes()
                    ));
        }
    }

    private String createErrorResponse(String message, String code) {
        return String.format(
                "{\"message\":\"%s\",\"code\":\"%s\",\"timestamp\":\"%s\"}",
                message,
                code,
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME)
        );
    }
}

@Data
class InvoiceDetailsDTO {
    private Invoice invoice;
    private PaymentHistory paymentHistory;
    private List<ProductDetailsDTO> products;
}

@Data
class ProductDetailsDTO {
    private String id;
    private String name;
    private String description;
    private BigDecimal price;
    private int quantity;
    private BigDecimal subtotal;
}

class PdfGenerationException extends RuntimeException {
    public PdfGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
