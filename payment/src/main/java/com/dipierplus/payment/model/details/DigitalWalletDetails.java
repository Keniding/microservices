package com.dipierplus.payment.model.details;

import com.dipierplus.payment.model.type.DigitalWalletType;
import com.dipierplus.payment.model.PaymentDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DigitalWalletDetails implements PaymentDetails {
    private String phoneNumber;
    private DigitalWalletType walletType;
}
