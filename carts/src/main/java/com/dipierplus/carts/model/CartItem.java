package com.dipierplus.carts.model;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class CartItem {
    @Id
    private String id;
    private String productId;
    private int quantity;
}
