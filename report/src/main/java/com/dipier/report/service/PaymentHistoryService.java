package com.dipier.report.service;

import com.dipier.report.model.PaymentHistory;

import java.util.List;

public interface PaymentHistoryService {
    List<PaymentHistory> getAllPaymentHistory();
    PaymentHistory createPaymentHistory(PaymentHistory paymentHistory);
    PaymentHistory getPaymentHistory(String id);
    PaymentHistory updatePaymentHistory(PaymentHistory paymentHistory);
    void deletePaymentHistory(String id);
}
