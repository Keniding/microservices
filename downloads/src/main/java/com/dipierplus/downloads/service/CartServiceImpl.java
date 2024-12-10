package com.dipierplus.downloads.service;

import com.dipierplus.downloads.model.Cart;
import com.dipierplus.downloads.model.CartItem;
import com.dipierplus.downloads.repository.CartItemRepository;
import com.dipierplus.downloads.repository.CartRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class CartServiceImpl {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ConcurrentHashMap<String, CompletableFuture<BigDecimal>> priceListeners = new ConcurrentHashMap<>();

    public List<CartItem> getCartItems(String customerId) {
        return cartRepository.findByCustomerId(customerId)
                .map(Cart::getItems)
                .orElseGet(List::of);
    }

    public Cart getCart(String customerId) {
        return cartRepository.findByCustomerId(customerId).orElse(null);
    }

    public boolean isProductInCart(String customerId, String productId) {
        return cartRepository.findByCustomerId(customerId)
                .map(cart -> cart.getItems().stream()
                        .anyMatch(item -> item.getProductId().equals(productId)))
                .orElse(false);
    }
}