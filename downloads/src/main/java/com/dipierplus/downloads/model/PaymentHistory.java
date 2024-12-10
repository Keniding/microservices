package com.dipierplus.downloads.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
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
