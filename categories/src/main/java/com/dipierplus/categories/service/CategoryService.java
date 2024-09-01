package com.dipierplus.categories.service;

import com.dipierplus.categories.dto.CategoryRequest;
import com.dipierplus.categories.dto.CategoryResponse;
import com.dipierplus.categories.model.Category;
import com.dipierplus.categories.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public void createCategory(CategoryRequest categoryRequest) {
        Category category = Category.builder()
                .name(categoryRequest.getName())
                .build();
        categoryRepository.save(category);
        log.info("Category {} is saved", category.getId());
    }

    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(this::mapToCategoryResponse).toList();
    }

    public CategoryResponse getCategory(String id) {
        return categoryRepository.findById(id)
                .map(this::mapToCategoryResponse)
                .orElseGet(() -> {
                    log.info("Category not found with id: {}", id);
                    return null;
                });
    }

    public void updateCategory(String id, CategoryRequest categoryRequest) {
        if (getCategory(id) == null) {
            log.error("Error updating category, category not found with id: {}", id);
            return;
        }

        try {
            Category category = Category.builder()
                    .id(id)
                    .name(categoryRequest.getName())
                    .build();

            categoryRepository.save(category);
            log.info("Category {} updated successfully", id);
        } catch (Exception e) {
            log.error("Error updating category with id: {}", id, e);
        }
    }

    public void deleteCategory(String id) {
        if (getCategory(id) == null) {
            log.error("Error deleting category, category not found with id: {}", id);
            return;
        }

        try {
            categoryRepository.deleteById(id);
            log.info("Category {} deleted successfully", id);
        } catch (Exception e) {
            log.error("Error deleting category with id: {}", id, e);
        }
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}