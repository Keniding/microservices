package com.dipier.report.impl;

import com.dipier.report.model.PaymentHistory;
import com.dipier.report.repository.PaymentHistoryRepository;
import com.dipier.report.service.PaymentHistoryService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentHistoryServiceImp implements PaymentHistoryService {

    private final PaymentHistoryRepository paymentHistoryRepository;

    @Override
    public List<PaymentHistory> getAllPaymentHistory() {
        return paymentHistoryRepository.findAll();
    }

    @Override
    public PaymentHistory createPaymentHistory(PaymentHistory paymentHistory) {
        if (paymentHistory == null) {
            throw new IllegalArgumentException("Payment history cannot be null");
        }
        return paymentHistoryRepository.save(paymentHistory);
    }

    @Override
    public PaymentHistory getPaymentHistory(String id) {
        return paymentHistoryRepository.findById(id).orElseThrow(() -> new RuntimeException("Payment History not found"));
    }

    @Override
    public PaymentHistory updatePaymentHistory(PaymentHistory paymentHistory) {
        if (paymentHistory == null || paymentHistory.getId() == null) {
            throw new IllegalArgumentException("Payment history and ID cannot be null");
        }
        getPaymentHistory(paymentHistory.getId());
        return paymentHistoryRepository.save(paymentHistory);
    }

    @Override
    public void deletePaymentHistory(String id) {
        getPaymentHistory(id);
        paymentHistoryRepository.deleteById(id);
    }
}