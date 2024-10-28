package com.dipierplus.payment.service;


import com.dipierplus.payment.model.PaymentMethod;

import java.util.List;

public interface PaymentMethodService {
    List<PaymentMethod> getAllMethods(String customerId);
    PaymentMethod addPaymentMethod(PaymentMethod paymentMethod);
    PaymentMethod updatePaymentMethod(String methodId, PaymentMethod paymentMethod);
    void deletePaymentMethod(String methodId);
}
