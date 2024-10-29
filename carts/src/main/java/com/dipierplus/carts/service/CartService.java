package com.dipierplus.carts.service;

import com.dipierplus.carts.model.Cart;
import com.dipierplus.carts.model.CartItem;

import java.util.List;

public interface CartService {
    void addItemToCart(String customerId, CartItem item);
    void removeItemFromCart(String customerId, String productId);
    List<CartItem> getCartItems(String customerId);

    void updateItemQuantity(String customerId, String productId, int quantity);
    void clearCart(String customerId);
    Cart getCart(String customerId);
    boolean isProductInCart(String customerId, String productId);
    double getCartTotal(String customerId);
}
