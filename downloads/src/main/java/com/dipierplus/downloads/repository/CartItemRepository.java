package com.dipierplus.downloads.repository;

import com.dipierplus.downloads.model.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CartItemRepository extends MongoRepository<CartItem, String> {
}
