package com.dipierplus.downloads.service;

import com.dipierplus.downloads.model.PaymentHistory;
import com.dipierplus.downloads.repository.PaymentHistoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentHistoryServiceImp {

    private final PaymentHistoryRepository paymentHistoryRepository;

    public List<PaymentHistory> getAllPaymentHistory() {
        return paymentHistoryRepository.findAll();
    }

    public PaymentHistory createPaymentHistory(PaymentHistory paymentHistory) {
        if (paymentHistory == null) {
            throw new IllegalArgumentException("Payment history cannot be null");
        }
        return paymentHistoryRepository.save(paymentHistory);
    }

    public PaymentHistory getPaymentHistory(String id) {
        return paymentHistoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment History not found"));
    }

    public PaymentHistory getInvoicePaymentHistory(String id) {
        return paymentHistoryRepository.findByInvoiceId(id).orElseThrow(() -> new RuntimeException("Payment History not found"));
    }
}