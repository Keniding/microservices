package com.dipierplus.inventory.controller;

import com.dipierplus.inventory.model.Inventory;
import com.dipierplus.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<?> getAllInventory() {
        List<Inventory> list = inventoryService.getListInventory();
        return ResponseEntity.ok(list);
    }

    @PostMapping()
    public ResponseEntity<String> createOrUpdateInventory(@RequestParam String skuCode) {
        try {
            inventoryService.createOrUpdateInventory(skuCode);
            return ResponseEntity.ok("Inventario actualizado exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear inventario: " + e.getMessage());
        }
    }

    @GetMapping("/{skuCode}/stock")
    public ResponseEntity<Boolean> isInStock(@PathVariable("skuCode") String skuCode) {
        try {
            boolean inStock = inventoryService.isInStock(skuCode);
            return ResponseEntity.ok(inStock);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{skuCode}")
    public ResponseEntity<?> getInventory(@PathVariable("skuCode") String skuCode) {
        try {
            Optional<Inventory> inventory = inventoryService.getInventory(skuCode);
            return inventory.map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.noContent().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No se encontró el inventario específico: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Solicitud incorrecta: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error del servidor: " + e.getMessage());
        }
    }

    @PutMapping("/increase")
    public ResponseEntity<String> increaseStock(@RequestParam String skuCode, @RequestParam int quantity) {
        try {
            inventoryService.increaseStock(skuCode, quantity);
            return ResponseEntity.ok("Stock incrementado exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al incrementar stock: " + e.getMessage());
        }
    }

    @PutMapping("/decrease")
    public ResponseEntity<String> decreaseStock(@RequestParam String skuCode, @RequestParam int quantity) {
        try {
            inventoryService.decreaseStock(skuCode, quantity);
            return ResponseEntity.ok("Stock decrementado exitosamente");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al decrementar stock: " + e.getMessage());
        }
    }

    @GetMapping("/lowQuantity")
    public ResponseEntity<?> lowQuantity(@RequestParam int quantity) {
        try {
            List<Inventory> lowStockItems = inventoryService.getLowStockInventory(quantity);
            if (lowStockItems.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(lowStockItems);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body("No se encontró el inventario: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body("Solicitud incorrecta: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error del servidor: " + e.getMessage());
        }
    }
}
