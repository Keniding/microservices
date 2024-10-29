package com.dipierplus.invoice.impl;

import com.dipierplus.invoice.dto.PaymentDTO;
import com.dipierplus.invoice.event.BillingPriceTotalRequestEvent;
import com.dipierplus.invoice.event.BillingPriceTotalResponseEvent;
import com.dipierplus.invoice.event.PaymentCompletedEvent;
import com.dipierplus.invoice.model.Invoice;
import com.dipierplus.invoice.model.PaymentHistory;
import com.dipierplus.invoice.repository.InvoiceRepository;
import com.dipierplus.invoice.repository.PaymentHistoryRepository;
import com.dipierplus.invoice.service.BillingService;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BillingServiceImp implements BillingService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final RabbitTemplate rabbitTemplate;

    @Override
    public ArrayList<Invoice> getAllInvoices(String customerId) {
        return invoiceRepository.findByCustomerId(customerId);
    }

    @Override
    public Invoice createInvoice(Invoice invoice) {
        if (invoice == null || invoice.getCustomerId() == null) {
            throw new IllegalArgumentException("Invoice or customer ID cannot be null.");
        }
        return invoiceRepository.save(invoice);
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

    @RabbitListener(queues = "billingQueue")
    public void handleCartTotalResponse(BillingPriceTotalResponseEvent responseEvent) {
        BigDecimal cartTotal = BigDecimal.valueOf(responseEvent.getTotal());

        System.out.println("Total recibido del carrito: " + cartTotal);

        if (cartTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El total del carrito es inválido.");
        }

        Invoice invoice = new Invoice();
        invoice.setCustomerId(responseEvent.getCustomerId());
        invoice.setTotalAmount(cartTotal);
        invoice.setPaymentDate(LocalDateTime.now());
        invoice.setStatus("PAGADO");
        invoiceRepository.save(invoice);

        PaymentHistory paymentHistory = new PaymentHistory();
        paymentHistory.setInvoiceId(invoice.getId());
        paymentHistory.setAmount(cartTotal);
        paymentHistory.setPaymentDate(LocalDateTime.now());
        paymentHistoryRepository.save(paymentHistory);

        PaymentCompletedEvent completedEvent = new PaymentCompletedEvent(responseEvent.getCustomerId(), invoice.getId(), cartTotal);
        rabbitTemplate.convertAndSend("appExchange", "payment.completed", completedEvent);
    }

    @Override
    public void calculateAndProcessInvoice(String customerId) {
        BillingPriceTotalRequestEvent event = new BillingPriceTotalRequestEvent(customerId);
        rabbitTemplate.convertAndSend("appExchange", "billing.request", event);
        System.out.println("Request event sent for customer: " + customerId);
    }
}