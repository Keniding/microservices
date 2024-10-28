package com.dipierplus.payment.model.details;

import com.dipierplus.payment.model.type.CurrencyType;
import com.dipierplus.payment.model.PaymentDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashPaymentDetails implements PaymentDetails {
    private CurrencyType currency; // Moneda del pago
}
