package com.dipierplus.carts.imp;

import com.dipierplus.carts.event.BillingPriceTotalRequestEvent;
import com.dipierplus.carts.event.BillingPriceTotalResponseEvent;
import com.dipierplus.carts.event.ProductPriceRequestEvent;
import com.dipierplus.carts.event.ProductPriceResponseEvent;
import com.dipierplus.carts.model.Cart;
import com.dipierplus.carts.model.CartItem;
import com.dipierplus.carts.model.CartStatus;
import com.dipierplus.carts.repository.CartItemRepository;
import com.dipierplus.carts.repository.CartRepository;
import com.dipierplus.carts.service.CartService;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ConcurrentHashMap<String, CompletableFuture<BigDecimal>> priceListeners = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public void addItemToCart(String customerId, CartItem item) {
        Cart cart = getOrCreateCart(customerId);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(cartItem -> cartItem.getProductId().equals(item.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem itemToUpdate = existingItem.get();
            itemToUpdate.setQuantity(itemToUpdate.getQuantity() + item.getQuantity());
            cartItemRepository.save(itemToUpdate);
        } else {
            cart.getItems().add(item);
            cartItemRepository.save(item);
        }

        cartRepository.save(cart);
    }

    @Override
    @Transactional
    public void removeItemFromCart(String customerId, String productId) {
        Optional<Cart> cartOptional = cartRepository.findByCustomerId(customerId);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();

            Optional<CartItem> itemToRemove = cart.getItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst();

            if (itemToRemove.isPresent()) {
                cart.getItems().remove(itemToRemove.get());

                cartRepository.save(cart);
                cartItemRepository.deleteById(itemToRemove.get().getId());
            }
        }
    }

    @Override
    public List<CartItem> getCartItems(String customerId) {
        return cartRepository.findByCustomerId(customerId)
                .map(Cart::getItems)
                .orElseGet(List::of);
    }

    @Override
    @Transactional
    public void updateItemQuantity(String customerId, String productId, int quantity) {
        Optional<Cart> cartOptional = cartRepository.findByCustomerId(customerId);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            cart.getItems().stream()
                    .filter(item -> item.getProductId().equals(productId))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setQuantity(quantity);
                        cartItemRepository.save(item);
                        cartRepository.save(cart);
                    });
        } else {
            throw new NoSuchElementException("Cart not found for customer ID: " + customerId);
        }
    }

    @Override
    @Transactional
    public void clearCart(String customerId) {
        Optional<Cart> cartOptional = cartRepository.findByCustomerId(customerId);
        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            if (cart.getStatus().equals(CartStatus.ACTIVE)) {
                List<CartItem> itemsToDelete = new ArrayList<>(cart.getItems());
                cart.getItems().clear();
                cartRepository.save(cart);
                cartItemRepository.deleteAll(itemsToDelete);
            }
        } else {
            throw new NoSuchElementException("Cart not found for customer ID: " + customerId);
        }
    }

    @Override
    public Cart getCart(String customerId) {
        return cartRepository.findByCustomerId(customerId).orElse(null);
    }

    @Override
    public boolean isProductInCart(String customerId, String productId) {
        return cartRepository.findByCustomerId(customerId)
                .map(cart -> cart.getItems().stream()
                        .anyMatch(item -> item.getProductId().equals(productId)))
                .orElse(false);
    }

    @Override
    public double getCartTotal(String customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId).orElse(new Cart());
        List<CompletableFuture<BigDecimal>> priceFutures = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            CompletableFuture<BigDecimal> priceFuture = rabbitEventGetPriceProduct(item.getProductId())
                    .handle((price, throwable) -> {
                        if (throwable != null) {
                            System.err.println("Error al obtener el precio para el producto: " + item.getProductId() + " - " + throwable.getMessage());
                            return BigDecimal.ZERO;
                        }
                        if (price == null) {
                            System.err.println("El precio es null para el producto: " + item.getProductId());
                            return BigDecimal.ZERO;
                        }
                        return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                    });
            priceFutures.add(priceFuture);
        }

        return priceFutures.stream()
                .map(CompletableFuture::join)
                .map(BigDecimal::doubleValue)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    private CompletableFuture<BigDecimal> rabbitEventGetPriceProduct(String productId) {
        CompletableFuture<BigDecimal> priceFuture = new CompletableFuture<>();

        CompletableFuture<BigDecimal> existingFuture = priceListeners.get(productId);
        if (existingFuture != null && !existingFuture.isDone()) {
            return existingFuture;
        }

        try {
            ProductPriceRequestEvent event = new ProductPriceRequestEvent(productId);
            priceListeners.put(productId, priceFuture);
            rabbitTemplate.convertAndSend("appExchange", "product.price.request", event);

            setTimeout(() -> {
                if (!priceFuture.isDone()) {
                    System.err.println("Timeout esperando precio para: " + productId);
                    priceListeners.remove(productId);
                    priceFuture.complete(BigDecimal.ZERO);
                }
            });

        } catch (Exception e) {
            System.err.println("Error enviando solicitud de precio: " + e.getMessage());
            priceFuture.complete(BigDecimal.ZERO);
        }

        return priceFuture;
    }

    private void setTimeout(Runnable runnable) {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                runnable.run();
            } catch (Exception e) {
                System.err.println("Error en timeout: " + e.getMessage());
            }
        }).start();
    }

    @RabbitListener(queues = "product.price.response")
    public void handlePriceResponse(ProductPriceResponseEvent response) {
        CompletableFuture<BigDecimal> priceFuture = priceListeners.get(response.getProductId());
        if (priceFuture != null && !priceFuture.isDone()) {
            try {
                BigDecimal price = new BigDecimal(response.getPrice().toString());
                if (price.compareTo(BigDecimal.ZERO) > 0) {
                    priceFuture.complete(price);
                } else {
                    System.err.println("Precio inválido recibido: " + response.getPrice());
                    priceFuture.complete(BigDecimal.ZERO);
                }
            } catch (Exception e) {
                System.err.println("Error procesando precio: " + e.getMessage());
                priceFuture.complete(BigDecimal.ZERO);
            } finally {
                priceListeners.remove(response.getProductId());
            }
        }
    }

    @RabbitListener(queues = "billingQueue")
    public void handlePriceRequest(BillingPriceTotalRequestEvent request) {
        Cart cart = getCartCustomerId(request.getCustomerId());
        if (cart == null) {
            System.err.println("No se encontró el carrito para el customerId:" + request.getCustomerId());
            return;
        }

        BillingPriceTotalResponseEvent response = new BillingPriceTotalResponseEvent(
                cart.getId(),
                cart.getCustomerId(),
                getCartTotal(cart.getCustomerId())
        );
        rabbitTemplate.convertAndSend("appExchange", "billing.cart", response);
        System.out.println("Datos: " + response);
    }

    private Cart getCartCustomerId(String cartId) {
        return cartRepository.findByCustomerId(cartId)
                .orElse(null);
    }

    private Cart getOrCreateCart(String customerId) {
        return cartRepository.findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    Cart newCart = new Cart(customerId);
                    newCart.setStatus(CartStatus.ACTIVE);
                    newCart.setCreatedDate(new Date());
                    cartRepository.save(newCart);
                    return newCart;
                });
    }
}