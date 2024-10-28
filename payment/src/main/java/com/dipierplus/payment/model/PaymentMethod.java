package com.dipierplus.payment.model;

import com.dipierplus.payment.model.type.PaymentMethodType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@Document(collection = "payment_methods")
public class PaymentMethod {

    @Id
    private String id;
    private String customerId;
    private PaymentMethodType methodType;
    private List<PaymentDetails> details;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date transactionDate;
}
