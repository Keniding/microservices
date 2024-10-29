package com.dipierplus.invoice.dto;

import lombok.Data;

@Data
public class PaymentDTO {
    private String customerId;
    private String paymentMethodId;
}
