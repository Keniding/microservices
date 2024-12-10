package com.dipierplus.downloads.service;

import com.dipierplus.downloads.model.Invoice;
import com.dipierplus.downloads.repository.InvoiceRepository;
import com.dipierplus.downloads.repository.PaymentHistoryRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class BillingServiceImp {

    private static final Logger logger = LoggerFactory.getLogger(BillingServiceImp.class);

    private final InvoiceRepository invoiceRepository;

    public ArrayList<Invoice> getAllInvoices(String customerId) {
        return invoiceRepository.findByCustomerId(customerId);
    }

    public Invoice getInvoice(String id) {
        Optional<Invoice> invoice = invoiceRepository.findById(id);
        return invoice.orElseThrow(() -> new NoSuchElementException("Invoice not found for ID: " + id));
    }
}