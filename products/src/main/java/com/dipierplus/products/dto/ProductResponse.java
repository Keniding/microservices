package com.dipierplus.products.dto;

import com.dipierplus.products.model.Category;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private String id;
    private String name;
    private String description;
    private Integer stock;
    private BigDecimal price;
    private ArrayList<Category> categories;
}
