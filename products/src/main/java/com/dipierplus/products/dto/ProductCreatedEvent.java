package com.dipierplus.products.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductCreatedEvent implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String skuCode;
}