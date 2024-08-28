package com.dipierplus.products.dto;

import com.dipierplus.products.model.Category;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductRequest {
    private String name;
    private String description;
    private String skuCode;
    private BigDecimal price;private ArrayList<Category> categories;
}
