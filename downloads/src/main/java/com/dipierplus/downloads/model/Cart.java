package com.dipierplus.downloads.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Cart {
    @Id
    private String id;
    private String customerId;
    private List<CartItem> items = new ArrayList<>();
    private CartStatus status;
    private Date createdDate;

    public Cart(String customerId) {
        this.customerId = customerId;
        this.items = new ArrayList<>();
    }
}
