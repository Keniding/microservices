package com.dipierplus.inventory.repository;

import com.dipierplus.inventory.model.Inventory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface InventoryRepository extends MongoRepository<Inventory, String> {

    Optional<Inventory> findBySkuCode(String skCode);
}
