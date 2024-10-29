package com.dipierplus.invoice.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartTotalRequestEvent {
    private String customerId;
}

