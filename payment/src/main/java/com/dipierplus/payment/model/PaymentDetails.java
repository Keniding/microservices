package com.dipierplus.payment.model;

import com.dipierplus.payment.model.details.BankTransferDetails;
import com.dipierplus.payment.model.details.CardDetails;
import com.dipierplus.payment.model.details.CashPaymentDetails;
import com.dipierplus.payment.model.details.DigitalWalletDetails;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CardDetails.class, name = "CARD"),
        @JsonSubTypes.Type(value = DigitalWalletDetails.class, name = "WALLET"),
        @JsonSubTypes.Type(value = BankTransferDetails.class, name = "BANK_ACCOUNT"),
        @JsonSubTypes.Type(value = CashPaymentDetails.class, name = "CASH")
})
public interface PaymentDetails {
}
