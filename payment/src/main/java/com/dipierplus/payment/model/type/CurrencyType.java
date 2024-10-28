package com.dipierplus.payment.model.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum CurrencyType {
    EUR("EUR"),
    USD("USD"),
    SOL("SOL");

    private String value;
}
