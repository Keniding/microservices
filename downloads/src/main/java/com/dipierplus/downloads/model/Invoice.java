package com.dipierplus.downloads.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Document(collection = "invoices")
public class Invoice {
    @Id
    private String id;
    private String customerId;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime paymentDate;
}
