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
                .stock(productRequest.getStock())
                .price(productRequest.getPrice())
                .build();
        productRepository.save(product);
        log.info("Product {} is save", product.getId());
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(this::mapToProductResponse).toList();
    }

    public ProductResponse getProduct(String id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()){
            log.info("Product dont found {}", id);
            throw new ProductNotFoundException(id);
        }

        return ProductResponse.builder()
                .id(product.get().getId())
                .name(product.get().getName())
                .description(product.get().getDescription())
                .stock(product.get().getStock())
                .price(product.get().getPrice())
                .build();
    }

    private ProductResponse mapToProductResponse(@NotNull Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .stock(product.getStock())
                .price(product.getPrice())
                .build();
    }

    public void updateProduct(String id, ProductRequest productRequest) {
        if (getProduct(id) == null) {
            log.error("Error update {}", id);
            throw new ProductNotUpdateException(id);
        }

        Product product = Product.builder()
                .id(id)
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .stock(productRequest.getStock())
                .price(productRequest.getPrice())
                .build();

        productRepository.save(product);
    }

    public void deleteProduct(String id) {
        if (getProduct(id) == null) {
            log.error("Error delete {}", id);
            throw new ProductNotFoundException(id);
        }

        productRepository.deleteById(id);
    }
}
