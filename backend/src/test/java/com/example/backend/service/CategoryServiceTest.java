package com.example.backend.service;

import com.example.backend.dto.request.CategoryCreationRequest;
import com.example.backend.dto.response.CategoryResponse;
import com.example.backend.entity.Category;
import com.example.backend.entity.Product;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.mapper.CategoryMapper;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    CategoryMapper categoryMapper;

    @InjectMocks
    CategoryService categoryService;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .categoryId("cat-1")
                .name("electronics")
                .build();
    }

    @Test
    @SuppressWarnings("null")
    void createCategory_success() {
        CategoryCreationRequest request = CategoryCreationRequest.builder()
                .name("Electronics")
                .build();

        Category newCategory = Category.builder()
                .name("electronics")
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .categoryId("cat-1")
                .name("electronics")
                .build();

        Category savedCategory = Category.builder()
                .categoryId("cat-1")
                .name("electronics")
                .build();

        when(categoryRepository.findByName("electronics")).thenReturn(Optional.empty());
        when(categoryMapper.toCategory(any(CategoryCreationRequest.class))).thenReturn(newCategory);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryMapper.toCategoryResponse(savedCategory)).thenReturn(response);

        CategoryResponse result = categoryService.createCategory(request);

        assertNotNull(result);
        assertEquals("electronics", result.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_alreadyExists() {
        CategoryCreationRequest request = CategoryCreationRequest.builder()
                .name("Electronics")
                .build();

        when(categoryRepository.findByName("electronics")).thenReturn(Optional.of(testCategory));

        AppException exception = assertThrows(AppException.class, () -> {
            categoryService.createCategory(request);
        });

        assertEquals(ErrorCode.CATEGORY_EXISTED, exception.getErrorCode());
    }

    @Test
    void getAllCategories_success() {
        Category category2 = Category.builder()
                .categoryId("cat-2")
                .name("clothing")
                .build();

        CategoryResponse response1 = CategoryResponse.builder()
                .categoryId("cat-1")
                .name("electronics")
                .build();
        CategoryResponse response2 = CategoryResponse.builder()
                .categoryId("cat-2")
                .name("clothing")
                .build();

        when(categoryRepository.findAll()).thenReturn(List.of(testCategory, category2));
        when(categoryMapper.toCategoryResponse(testCategory)).thenReturn(response1);
        when(categoryMapper.toCategoryResponse(category2)).thenReturn(response2);

        List<CategoryResponse> results = categoryService.getAllCategories();

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void getCategoryById_success() {
        CategoryResponse response = CategoryResponse.builder()
                .categoryId("cat-1")
                .name("electronics")
                .build();

        when(categoryRepository.findById("cat-1")).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toCategoryResponse(testCategory)).thenReturn(response);

        CategoryResponse result = categoryService.getCategoryById("cat-1");

        assertNotNull(result);
        assertEquals("cat-1", result.getCategoryId());
    }

    @Test
    void getCategoryById_notFound() {
        when(categoryRepository.findById("non-existent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            categoryService.getCategoryById("non-existent");
        });

        assertEquals(ErrorCode.CATEGORY_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    @SuppressWarnings("null")
    void deleteCategory_success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::requireAdmin).thenAnswer(invocation -> null);

            Category categoryWithNoProducts = Category.builder()
                    .categoryId("cat-1")
                    .name("electronics")
                    .products(null)
                    .build();

            when(categoryRepository.findByName("electronics")).thenReturn(Optional.of(categoryWithNoProducts));
            doNothing().when(categoryRepository).delete(categoryWithNoProducts);

            categoryService.deleteCategory("Electronics");

            verify(categoryRepository).delete(categoryWithNoProducts);
        }
    }

    @Test
    void deleteCategory_notFound() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::requireAdmin).thenAnswer(invocation -> null);

            when(categoryRepository.findByName("non-existent")).thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class, () -> {
                categoryService.deleteCategory("non-existent");
            });

            assertEquals(ErrorCode.CATEGORY_NOT_EXIST, exception.getErrorCode());
        }
    }

    @Test
    void deleteCategory_usedByProduct() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::requireAdmin).thenAnswer(invocation -> null);

            // Create a mock product to make the set non-empty
            Product mockProduct = mock(Product.class);
            Category categoryWithProducts = Category.builder()
                    .categoryId("cat-1")
                    .name("electronics")
                    .products(Set.of(mockProduct)) // Non-empty set
                    .build();

            when(categoryRepository.findByName("electronics")).thenReturn(Optional.of(categoryWithProducts));

            AppException exception = assertThrows(AppException.class, () -> {
                categoryService.deleteCategory("Electronics");
            });

            assertEquals(ErrorCode.CATEGORY_USED_BY_PRODUCT, exception.getErrorCode());
        }
    }

    @Test
    void searchCategoriesByKeyword_success() {
        CategoryResponse response = CategoryResponse.builder()
                .categoryId("cat-1")
                .name("electronics")
                .build();

        when(categoryRepository.findByNameContainingIgnoreCase("elect"))
                .thenReturn(List.of(testCategory));
        when(categoryMapper.toCategoryResponse(testCategory)).thenReturn(response);

        List<CategoryResponse> results = categoryService.searchCategoriesByKeyword("Elect");

        assertNotNull(results);
        assertEquals(1, results.size());
    }
}
