package com.dipierplus.invoice.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "payment_history")
public class PaymentHistory {

    @Id
    private String id;
    private String invoiceId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
}
