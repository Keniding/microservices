package com.dipierplus.inventory.service;

import com.dipierplus.inventory.model.Inventory;
import com.dipierplus.inventory.repository.InventoryRepository;
import com.dipierplus.inventory.util.ProductCreatedEvent;
import com.dipierplus.rabbitmq.componet.MessageReceiver;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(MessageReceiver.class);
    private final InventoryRepository inventoryRepository;

    @RabbitListener(queues = "productQueue")
    public void handleProductCreated(ProductCreatedEvent event) {
        createOrUpdateInventory(event.getSkuCode());
    }

    public List<Inventory> getListInventory() {
        return inventoryRepository.findAll();
    }

    public boolean isInStock(String skuCode) {
        return inventoryRepository.findBySkuCode(skuCode)
                .map(inventory -> inventory.getQuantity() > 0)
                .orElse(false);
    }

    public Optional<Inventory> getInventory(String skuCode) {
        return inventoryRepository.findBySkuCode(skuCode);
    }

    public void increaseStock(String skuCode, int quantity) {
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseGet(() -> new Inventory(skuCode, 0));
        inventory.setQuantity(inventory.getQuantity() + quantity);
        inventoryRepository.save(inventory);
    }

    public void decreaseStock(String skuCode, int quantity) {
        Inventory inventory = inventoryRepository.findBySkuCode(skuCode)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        if (inventory.getQuantity() < quantity) {
            throw new IllegalStateException("Stock insuficiente");
        }
        inventory.setQuantity(inventory.getQuantity() - quantity);
        inventoryRepository.save(inventory);
    }

    public void createOrUpdateInventory(String skuCode) {
        inventoryRepository.findBySkuCode(skuCode)
                .ifPresentOrElse(
                        inventory -> {
                            logger.info("Inventario existente encontrado para SKU: {}. Cantidad actual: {}", skuCode, inventory.getQuantity());
                        },
                        () -> {
                            Inventory newInventory = new Inventory(skuCode, 0);
                            inventoryRepository.save(newInventory);
                            logger.info("Nuevo inventario creado para SKU: {} con cantidad inicial 0", skuCode);
                        }
                );
    }

    public List<Inventory> getLowStockInventory(int quantity) {
        return inventoryRepository.findByQuantityLessThan(quantity);
    }
}
