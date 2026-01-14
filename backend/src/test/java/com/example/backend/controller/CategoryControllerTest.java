package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.dto.request.CategoryCreationRequest;
import com.example.backend.dto.response.CategoryResponse;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.service.CategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @MockitoBean
    CategoryService categoryService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createCategory_success() throws Exception {
        CategoryCreationRequest request = CategoryCreationRequest.builder()
                .name("Electronics")
                .build();

        CategoryResponse response = CategoryResponse.builder()
                .categoryId("cat-1")
                .name("Electronics")
                .build();

        when(categoryService.createCategory(any()))
                .thenReturn(response);

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Electronics"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void createCategory_forbidden() throws Exception {
        CategoryCreationRequest request = new CategoryCreationRequest("Electronics");

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createCategory_existed() throws Exception {
        when(categoryService.createCategory(any()))
                .thenThrow(new AppException(ErrorCode.CATEGORY_EXISTED));

        mockMvc.perform(post("/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CategoryCreationRequest("Electronics")
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(ErrorCode.CATEGORY_EXISTED.getMessage()));
    }


    @Test
    void getAllCategories_success() throws Exception {
        CategoryResponse cat1 = CategoryResponse.builder()
                .categoryId("cat-1")
                .name("Electronics")
                .build();
        CategoryResponse cat2 = CategoryResponse.builder()
                .categoryId("cat-2")
                .name("Clothing")
                .build();

        when(categoryService.getAllCategories())
                .thenReturn(List.of(cat1, cat2));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2));
    }

    @Test
    void getCategoryById_notExist() throws Exception {
        when(categoryService.getCategoryById("x"))
                .thenThrow(new AppException(ErrorCode.CATEGORY_NOT_EXIST));

        mockMvc.perform(get("/categories/id/x"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value(ErrorCode.CATEGORY_NOT_EXIST.getMessage()));
    }


    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategory_success() throws Exception {
        doNothing().when(categoryService).deleteCategory("Electronics");

        mockMvc.perform(delete("/categories/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("deleted category"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategory_notExist() throws Exception {
        doThrow(new AppException(ErrorCode.CATEGORY_NOT_EXIST))
                .when(categoryService).deleteCategory("Unknown");

        mockMvc.perform(delete("/categories/Unknown"))
                .andExpect(status().isBadRequest());
    }


    @Test
    void getCategoryById_success() throws Exception {
        CategoryResponse response = CategoryResponse.builder()
                .categoryId("cat-1")
                .name("Electronics")
                .build();

        when(categoryService.getCategoryById("cat-1"))
                .thenReturn(response);

        mockMvc.perform(get("/categories/id/cat-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Electronics"));
    }

    @Test
    void getCategoryByKeyword_success() throws Exception {
        CategoryResponse response = CategoryResponse.builder()
                .categoryId("cat-1")
                .name("Electronics")
                .build();

        when(categoryService.searchCategoriesByKeyword("Elect"))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/categories/search/Elect"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));
    }


}
