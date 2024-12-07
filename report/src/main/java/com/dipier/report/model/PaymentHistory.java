package com.dipier.report.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payment_history")
public class PaymentHistory {

    @Id
    private String id;
    private String invoiceId;
    private BigDecimal amount;
    private LocalDateTime paymentDate;
}
