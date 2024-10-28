package com.dipierplus.carts.repository;

import com.dipierplus.carts.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CartItemRepository extends MongoRepository<CartItem, String> {
}
