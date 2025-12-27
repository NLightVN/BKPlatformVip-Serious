package com.example.backend.controller;

import com.example.backend.dto.request.CategoryCreationRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.CategoryResponse;
import com.example.backend.service.CategoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {
    CategoryService categoryService;

    @PostMapping
    ApiResponse<CategoryResponse> createCategory(@RequestBody CategoryCreationRequest request) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.createCategory(request))
                .build();
    }

    @GetMapping
    ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAllCategories())
                .build();
    }

    @DeleteMapping("/{categoryName}")
    ApiResponse<String> deleteCategory(@PathVariable String categoryName) {
        categoryService.deleteCategory(categoryName);
        return ApiResponse.<String>builder()
                .result("deleted category")
                .build();
    }

    @GetMapping("/id/{categoryId}")
    ApiResponse<CategoryResponse> getCategoryById(@PathVariable String categoryId) {
        return ApiResponse.<CategoryResponse>builder()
                .result(categoryService.getCategoryById(categoryId))
                .build();
    }

    @GetMapping("/search/{keyword}")
    ApiResponse<List<CategoryResponse>> getCategoryByKeyword(@PathVariable String keyword) {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.searchCategoriesBykeyword(keyword))
                .build();
    }
}
