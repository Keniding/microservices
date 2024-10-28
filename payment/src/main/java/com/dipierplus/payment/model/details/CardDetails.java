package com.dipierplus.payment.model.details;

import com.dipierplus.payment.model.PaymentDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardDetails implements PaymentDetails {
    private String cardNumber;
}
