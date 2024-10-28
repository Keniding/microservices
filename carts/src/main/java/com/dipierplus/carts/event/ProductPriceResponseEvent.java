package com.dipierplus.carts.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPriceResponseEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String productId;
    private BigDecimal price;
}
