package com.dipierplus.invoice.event;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentCompletedEvent {
    private String customerId;
    private String invoiceId;
    private BigDecimal amount;
}
