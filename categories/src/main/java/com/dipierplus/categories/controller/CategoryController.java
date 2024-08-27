package com.dipierplus.categories.controller;

import com.dipierplus.categories.dto.CategoryRequest;
import com.dipierplus.categories.dto.CategoryResponse;
import com.dipierplus.categories.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createCategory(@RequestBody CategoryRequest productRequest) {
        categoryService.createCategory(productRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public CategoryResponse getProduct(@PathVariable String id) {
        return categoryService.getCategory(id);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateCategory(@PathVariable String id, @RequestBody CategoryRequest productRequest) {
        try {
            categoryService.updateCategory(id, productRequest);
            return ResponseEntity.ok("Product updated successfully");
        }  catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
    }
}
