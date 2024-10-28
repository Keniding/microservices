package com.dipierplus.carts.controller;

import com.dipierplus.carts.model.Cart;
import com.dipierplus.carts.model.CartItem;
import com.dipierplus.carts.service.CartService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    @PostMapping("/{customerId}/add")
    public ResponseEntity<Void> addItem(@PathVariable String customerId, @RequestBody CartItem item) {
        cartService.addItemToCart(customerId, item);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{customerId}/remove")
    public ResponseEntity<Void> removeItem(@PathVariable String customerId, @RequestBody CartItem item) {
        cartService.removeItemFromCart(customerId, item.getProductId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{customerId}/items")
    public ResponseEntity<List<CartItem>> getCartItems(@PathVariable String customerId) {
        List<CartItem> items = cartService.getCartItems(customerId);
        return ResponseEntity.ok(items);
    }

    @PutMapping("/{customerId}/update/{productId}")
    public ResponseEntity<Void> updateItemQuantity(@PathVariable String customerId,
                                                   @PathVariable String productId,
                                                   @RequestParam int quantity) {
        cartService.updateItemQuantity(customerId, productId, quantity);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{customerId}/clear")
    public ResponseEntity<Void> clearCart(@PathVariable String customerId) {
        cartService.clearCart(customerId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{customerId}/cart")
    public ResponseEntity<Cart> getCart(@PathVariable String customerId) {
        Cart cart = cartService.getCart(customerId);
        return ResponseEntity.ok(cart);
    }

    @GetMapping("/{customerId}/contains/{productId}")
    public ResponseEntity<Boolean> isProductInCart(@PathVariable String customerId,
                                                   @PathVariable String productId) {
        boolean exists = cartService.isProductInCart(customerId, productId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{customerId}/total")
    public ResponseEntity<Double> getCartTotal(@PathVariable String customerId) {
        double total = cartService.getCartTotal(customerId);
        return ResponseEntity.ok(total);
    }
}
