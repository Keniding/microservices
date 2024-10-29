package com.dipierplus.carts.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillingPriceTotalRequestEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String cartId;
}
