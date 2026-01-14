package com.example.backend.service;

import com.example.backend.dto.request.CategoryCreationRequest;
import com.example.backend.dto.response.CategoryResponse;
import com.example.backend.entity.Category;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.mapper.CategoryMapper;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.util.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.backend.entity.Product;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CategoryService {
    CategoryRepository categoryRepository;
    CategoryMapper categoryMapper;

    private String normalize(String name) {
        return name.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    public CategoryResponse createCategory(CategoryCreationRequest request) {
        String normalizedName = normalize(request.getName());
        categoryRepository.findByName(normalizedName)
                .ifPresent(c -> {
                    throw new AppException(ErrorCode.CATEGORY_EXISTED);
                });
        request.setName(normalizedName);
        Category category = categoryMapper.toCategory(request);
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse getCategoryById(String id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXIST));
        return categoryMapper.toCategoryResponse(category);
    }

    public void deleteCategory(String categoryName){
        SecurityUtil.requireAdmin();
        String normalizedName = normalize(categoryName);
        Category category = categoryRepository.findByName(normalizedName)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_EXIST));
        
        // Remove category from all products that use it
        if(category.getProducts() != null && !category.getProducts().isEmpty()){
            for(Product product : category.getProducts()){
                product.getCategories().remove(category);
            }
        }
        
        categoryRepository.delete(category);
    }

    public List<CategoryResponse> searchCategoriesByKeyword(String keyword) {
        String normalized = normalize(keyword);
        return categoryRepository.findByNameContainingIgnoreCase(normalized)
                .stream().map(categoryMapper::toCategoryResponse)
                .collect(Collectors.toList());
    }

}
