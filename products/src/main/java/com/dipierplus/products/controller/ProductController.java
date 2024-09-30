package com.dipierplus.products.controller;

import com.dipierplus.products.dto.*;
import com.dipierplus.products.exception.*;
import com.dipierplus.products.model.Category;
import com.dipierplus.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createProduct(@RequestBody ProductRequest productRequest) {
        productService.createProduct(productRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse getProduct(@PathVariable String id) {
        return productService.getProduct(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateProduct(@PathVariable String id, @RequestBody ProductRequest productRequest) {
        try {
            productService.updateProduct(id, productRequest);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Product updated successfully");
            return ResponseEntity.ok(response);
        } catch (ProductNotFoundException ex) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        } catch (ProductNotUpdateException ex) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
    }

    @GetMapping("/sku/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse getProductBySkuCode(@PathVariable String id) {
        return productService.getProductBySkuCode(id);
    }

    @PostMapping("/category/{id}/remove")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<String> modificationCategory(@PathVariable String id, @RequestBody ArrayList<Category> request) {
        try {
            productService.modificationCategory(id, request);
            return ResponseEntity.ok("Product Category updated successfully");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }
}
