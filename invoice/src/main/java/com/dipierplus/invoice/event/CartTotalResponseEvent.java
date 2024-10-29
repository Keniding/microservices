package com.dipierplus.invoice.event;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartTotalResponseEvent {
    private String customerId;
    private BigDecimal total;
}
