package com.dipierplus.downloads.repository;

import com.dipierplus.downloads.model.Cart;
import com.dipierplus.downloads.model.CartStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {
    Optional<Cart> findByCustomerId(String customerId);
    Optional<Cart> findByCustomerIdAndStatus(String customerId, CartStatus cartStatus);
}
