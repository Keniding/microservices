package com.dipierplus.products.exception;

public class ProductNotUpdateException extends RuntimeException {
    public ProductNotUpdateException(String id) {
        super("Product not update with id: " + id);
    }
}