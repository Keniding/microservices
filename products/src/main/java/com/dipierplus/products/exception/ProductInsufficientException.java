package com.dipierplus.products.exception;

public class ProductInsufficientException extends RuntimeException {
    public ProductInsufficientException(String id) {
        super("Product insufficient with id: " + id);
    }
}