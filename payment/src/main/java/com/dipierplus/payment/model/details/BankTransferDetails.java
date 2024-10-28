package com.dipierplus.payment.model.details;

import com.dipierplus.payment.model.PaymentDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankTransferDetails implements PaymentDetails {
    private String accountNumber;
    private String bankName;
}