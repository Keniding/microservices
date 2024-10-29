package com.dipierplus.invoice.service;

import com.dipierplus.invoice.dto.PaymentDTO;
import com.dipierplus.invoice.model.Invoice;

import java.util.ArrayList;

public interface BillingService {
    ArrayList<Invoice> getAllInvoices(String customerId);
    Invoice createInvoice(Invoice invoice);
    Invoice getInvoice(String id);
    Invoice updateInvoice(Invoice invoice);
    void deleteInvoice(String id);
    void calculateAndProcessInvoice(String customerId);
}
