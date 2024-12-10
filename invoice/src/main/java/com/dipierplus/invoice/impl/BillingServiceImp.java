package com.dipierplus.invoice.impl;

import com.dipierplus.invoice.event.BillingPriceTotalRequestEvent;
import com.dipierplus.invoice.event.BillingPriceTotalResponseEvent;
import com.dipierplus.invoice.event.PaymentCompletedEvent;
import com.dipierplus.invoice.model.Invoice;
import com.dipierplus.invoice.model.PaymentHistory;
import com.dipierplus.invoice.repository.InvoiceRepository;
import com.dipierplus.invoice.repository.PaymentHistoryRepository;
import com.dipierplus.invoice.service.BillingService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class BillingServiceImp implements BillingService {

    private static final Logger logger = LoggerFactory.getLogger(BillingServiceImp.class);

    private final InvoiceRepository invoiceRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ConcurrentHashMap<String, CompletableFuture<BigDecimal>> priceListeners = new ConcurrentHashMap<>();

    @Override
    public ArrayList<Invoice> getAllInvoices(String customerId) {
        return invoiceRepository.findByCustomerId(customerId);
    }

    @Override
    public Invoice createInvoice(Invoice invoice) {
        if (invoice == null || invoice.getCustomerId() == null) {
            throw new IllegalArgumentException("Invoice or customer ID cannot be null.");
        }

        invoice.setId(null);

        logger.info("Guardando factura: {}", invoice);

        Invoice savedInvoice = invoiceRepository.save(invoice);

        logger.info("Factura guardada: {}", savedInvoice);

        if (savedInvoice.getId() == null) {
            logger.error("MongoDB no generó ID para la factura");
            throw new RuntimeException("Error al generar ID de factura");
        }

        return savedInvoice;
    }

    @Override
    public Invoice getInvoice(String id) {
        Optional<Invoice> invoice = invoiceRepository.findById(id);
        return invoice.orElseThrow(() -> new NoSuchElementException("Invoice not found for ID: " + id));
    }

    @Override
    public Invoice updateInvoice(Invoice invoice) {
        if (invoice == null || invoice.getId() == null) {
            throw new IllegalArgumentException("Invoice or ID cannot be null.");
        }
        if (!invoiceRepository.existsById(invoice.getId())) {
            throw new NoSuchElementException("Invoice not found for ID: " + invoice.getId());
        }
        return invoiceRepository.save(invoice);
    }

    @Override
    public void deleteInvoice(String id) {
        if (!invoiceRepository.existsById(id)) {
            throw new NoSuchElementException("Invoice not found for ID: " + id);
        }
        invoiceRepository.deleteById(id);
    }

    private void rabbitEventGetPriceTotal(String customerId) {
        CompletableFuture<BigDecimal> priceFuture = new CompletableFuture<>();
        BillingPriceTotalRequestEvent event = new BillingPriceTotalRequestEvent(customerId);
        rabbitTemplate.convertAndSend("appExchange", "billing.cart", event);

        priceListeners.put(customerId, priceFuture);
    }

    @RabbitListener(queues = "billingQueue")
    public void handleCartTotalResponse(BillingPriceTotalResponseEvent response) {
        String cartId = response.getCartId();
        double total = response.getTotal();

        if (total != 0) {
            try {
                BigDecimal cartTotal = BigDecimal.valueOf(total);

                if (cartTotal.compareTo(BigDecimal.ZERO) <= 0) {
                    logger.warn("El total del carrito es inválido: {}", cartTotal);
                    return;
                }

                Optional<Invoice> pendingInvoice = Optional.ofNullable(invoiceRepository.findByCustomerIdAndStatus(
                        response.getCustomerId(),
                        "PENDING"
                ));

                Invoice invoice;
                if (pendingInvoice.isPresent()) {
                    // Actualizar factura existente
                    invoice = pendingInvoice.get();
                    invoice.setStatus("PAGADO");
                    invoice.setTotalAmount(cartTotal);
                    invoice.setPaymentDate(LocalDateTime.now());
                } else {
                    // Crear nueva factura solo si no existe una pendiente
                    invoice = new Invoice();
                    invoice.setCustomerId(response.getCustomerId());
                    invoice.setTotalAmount(cartTotal);
                    invoice.setPaymentDate(LocalDateTime.now());
                    invoice.setStatus("PAGADO");
                }

                invoiceRepository.save(invoice);

                PaymentHistory paymentHistory = new PaymentHistory();
                paymentHistory.setInvoiceId(invoice.getId());
                paymentHistory.setAmount(cartTotal);
                paymentHistory.setPaymentDate(LocalDateTime.now());
                paymentHistoryRepository.save(paymentHistory);

                PaymentCompletedEvent completedEvent = new PaymentCompletedEvent(response.getCustomerId(), invoice.getId(), cartTotal);
                rabbitTemplate.convertAndSend("appExchange", "payment.completed", completedEvent);

            } catch (Exception e) {
                logger.error("Error al procesar el evento de respuesta del carrito: ", e);
            }
        } else {
            logger.error("Total del carrito es cero o no válido: {}", cartId);
        }
    }

    @Override
    public void calculateAndProcessInvoice(String customerId) {
        try {
            BillingPriceTotalRequestEvent event = new BillingPriceTotalRequestEvent(customerId);
            rabbitEventGetPriceTotal(event.getCustomerId());
            logger.info("Request event sent for customer: {}", customerId);
        } catch (Exception e) {
            logger.error("Error sending billing request event for carrito: {}", customerId, e);
        }
    }
}