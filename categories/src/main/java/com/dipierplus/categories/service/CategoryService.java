package com.dipierplus.categories.service;

import com.dipierplus.categories.dto.CategoryRequest;
import com.dipierplus.categories.dto.CategoryResponse;
import com.dipierplus.categories.model.Category;
import com.dipierplus.categories.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public void createCategory(@NotNull CategoryRequest productRequest){
        Category product = Category.builder()
                .name(productRequest.getName())
                .build();
        categoryRepository.save(product);
        log.info("Category {} is save", product.getId());
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> products = categoryRepository.findAll();
        return products.stream().map(this::mapToCategoryResponse).toList();
    }

    private CategoryResponse mapToCategoryResponse(@NotNull Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public CategoryResponse getCategory(String id) {
        Optional<Category> product = categoryRepository.findById(id);
        if (product.isEmpty()){
            log.info("Category dont found {}", id);
            return null;
        }

        return CategoryResponse.builder()
                .id(product.get().getId())
                .name(product.get().getName())
                .build();
    }

    public void updateCategory(String id, CategoryRequest categoryRequest) {
        if (getCategory(id) == null) {
            log.error("Error update null {}", id);
        }

        try {
            Category product = Category.builder()
                    .id(id)
                    .name(categoryRequest.getName())
                    .build();

            categoryRepository.save(product);
        } catch (Exception e) {
            log.error("Error update {}", id);
        }

    }

    public void deleteCategory(String id) {
        if (getCategory(id) == null) {
            log.error("Error delete null {}", id);
            return;
        }

        try {
            categoryRepository.deleteById(id);
        }catch (Exception e) {
            log.error("Error delete {}", id);
        }
    }
}
