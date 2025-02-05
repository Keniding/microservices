package com.dipierplus.carts.event;

import com.dipierplus.carts.model.CartItem;
import com.dipierplus.carts.model.CartStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BillingPriceTotalResponseEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String cartId;
    private String customerId;
    private double total;
}
