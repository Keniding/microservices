package com.dipierplus.inventory.controller;

import com.dipierplus.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{skuCode}")
    @ResponseStatus(HttpStatus.OK)
    public boolean isInStock(@PathVariable("skuCode") String skuCode) {
        return inventoryService.isInStock(skuCode);
    }

    @PostMapping("/increase")
    public ResponseEntity<String> increaseStock(@RequestParam String skuCode, @RequestParam int quantity) {
        try {
            inventoryService.increaseStock(skuCode, quantity);
            return ResponseEntity.ok("Stock incrementado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al incrementar stock: " + e.getMessage());
        }
    }

    @PostMapping("/decrease")
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
}
