package com.dipierplus.products.service;

import com.dipierplus.products.dto.*;
import com.dipierplus.products.exception.ProductNotFoundException;
import com.dipierplus.products.exception.ProductNotUpdateException;
import com.dipierplus.products.model.Product;
import com.dipierplus.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public void createProduct(@NotNull ProductRequest productRequest){
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .skuCode(productRequest.getSkuCode())
                .price(productRequest.getPrice())
                .categories(productRequest.getCategories())
                .build();
        productRepository.save(product);
        log.info("Product {} is save", product.getId());
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::mapToProductResponse).toList();
    }

    private ProductResponse foundProduct(String id, @NotNull Optional<Product> product) {
        if (product.isEmpty()){
            log.info("Product dont found {}", id);
            throw new ProductNotFoundException(id);
        }

        return ProductResponse.builder()
                .id(product.get().getId())
                .name(product.get().getName())
                .description(product.get().getDescription())
                .price(product.get().getPrice())
                .skuCode(product.get().getSkuCode())
                .categories(product.get().getCategories())
                .build();
    }

    public ProductResponse getProduct(String id) {
        Optional<Product> product = productRepository.findById(id);
        return foundProduct(id, product);
    }

    private ProductResponse mapToProductResponse(@NotNull Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .skuCode(product.getSkuCode())
                .categories(product.getCategories())
                .build();
    }

    public void updateProduct(String id, ProductRequest productRequest) {
        if (getProduct(id) == null) {
            log.error("Error update null {}", id);
        }

        try {
            Product product = Product.builder()
                    .id(id)
                    .name(productRequest.getName())
                    .description(productRequest.getDescription())
                    .price(productRequest.getPrice())
                    .skuCode(productRequest.getSkuCode())
                    .categories(productRequest.getCategories())
                    .build();

            productRepository.save(product);
        } catch (Exception e) {
            log.error("Error update {}", id);
            throw new ProductNotUpdateException(id);
        }

    }

    public void deleteProduct(String id) {
        if (getProduct(id) == null) {
            log.error("Error delete null {}", id);
            throw new ProductNotFoundException(id);
        }

        try {
            productRepository.deleteById(id);
        }catch (Exception e) {
            log.error("Error delete {}", id);
        }
    }

    public ProductResponse getProductBySkuCode(String id) {
        Optional<Product> product = productRepository.findBySkuCode(id);
        return foundProduct(id, product);
    }

/*
    public void updateStock(String id, int quantity) {
        ProductResponse existingProduct = getProduct(id);

        if (existingProduct == null) {
            log.error("Error: product ID {} not found", id);
            return;
        }

        try {
            int currentStock = existingProduct.getStock();
            int newStock = calculateNewStock(currentStock, quantity);

            if (newStock < 0) {
                log.error("Error: insufficient stock for product ID {}", id);
                throw new ProductInsufficientException(id);
            }

            updateProductStock(existingProduct, newStock);
            log.info("Updated stock for product ID {}: new stock = {}", id, newStock);
        } catch (ProductInsufficientException e) {
            throw new ProductInsufficientException(id);
        } catch (Exception e) {
            log.error("Error updating stock for product ID {}: {}", id, e.getMessage());
        }
    }

    private int calculateNewStock(int currentStock, int quantity) {
        return currentStock - quantity;
    }

    private void updateProductStock(@NotNull ProductResponse existingProduct, int newStock) {
        Product product = Product.builder()
                .id(existingProduct.getId())
                .name(existingProduct.getName())
                .description(existingProduct.getDescription())
                .price(existingProduct.getPrice())
                .categories(existingProduct.getCategories())
                .build();

        productRepository.save(product);
    }
    */
}