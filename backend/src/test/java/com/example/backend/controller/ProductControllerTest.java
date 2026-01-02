package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.dto.request.ProductCreationRequest;
import com.example.backend.dto.response.ProductImageResponse;
import com.example.backend.dto.response.ProductResponse;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
@Import(SecurityConfig.class)
class ProductControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ProductService productService;

    @MockBean
    CustomJwtDecoder customJwtDecoder; // tránh SecurityConfig lỗi

    // ================= POST /products =================

    @Test
    @WithMockUser(username = "user1")
    void addProduct_success() throws Exception {
        ProductCreationRequest request = new ProductCreationRequest();
        request.setShopId("shop1");
        request.setName("Product A");
        request.setPrice(100);
        request.setBrand("Nike");
        request.setCategoryNames(Set.of("Shoes"));

        when(productService.createProduct(any()))
                .thenReturn(ProductResponse.builder()
                        .productId("p1")
                        .name("Product A")
                        .build());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.productId").value("p1"))
                .andExpect(jsonPath("$.result.name").value("Product A"));
    }

    @Test
    @WithMockUser
    void addProduct_shopNotExist() throws Exception {
        doThrow(new AppException(ErrorCode.SHOP_NOT_EXIST))
                .when(productService).createProduct(any());

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(ErrorCode.SHOP_NOT_EXIST.getMessage()));
    }

    // ================= GET /products/shop/{shopId} =================

    @Test
    void getAllProductsByShop_success() throws Exception {
        when(productService.getAllProducts("shop1"))
                .thenReturn(List.of(
                        ProductResponse.builder().productId("1").build(),
                        ProductResponse.builder().productId("2").build()
                ));

        mockMvc.perform(get("/products/shop/shop1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2));
    }

    // ================= GET /products/{productId} =================

    @Test
    void getProductById_success() throws Exception {
        when(productService.getProductById("p1"))
                .thenReturn(ProductResponse.builder()
                        .productId("p1")
                        .name("Product A")
                        .build());

        mockMvc.perform(get("/products/p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.productId").value("p1"));
    }

    @Test
    void getProductById_notExist() throws Exception {
        doThrow(new AppException(ErrorCode.PRODUCT_NOT_EXIST))
                .when(productService).getProductById("px");

        mockMvc.perform(get("/products/px"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(ErrorCode.PRODUCT_NOT_EXIST.getMessage()));
    }

    // ================= PUT /products/{productId} =================

    @Test
    @WithMockUser
    void updateProduct_success() throws Exception {
        when(productService.updateProduct(any(), eq("p1")))
                .thenReturn(ProductResponse.builder()
                        .productId("p1")
                        .name("Updated")
                        .build());

        mockMvc.perform(put("/products/p1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.name").value("Updated"));
    }

    // ================= DELETE /products/{productId} =================

    @Test
    @WithMockUser
    void deleteProduct_success() throws Exception {
        mockMvc.perform(delete("/products/p1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Product deleted"));
    }

    // ================= GET /products/category/{category} =================

    @Test
    void getProductsByCategory_success() throws Exception {
        when(productService.getProductsByCategory("shoes"))
                .thenReturn(List.of(ProductResponse.builder().productId("1").build()));

        mockMvc.perform(get("/products/category/shoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));
    }

    // ================= GET /products/brand/{brand} =================

    @Test
    void getProductsByBrand_success() throws Exception {
        when(productService.getProductsByBrand("nike"))
                .thenReturn(List.of(ProductResponse.builder().productId("1").build()));

        mockMvc.perform(get("/products/brand/nike"))
                .andExpect(status().isOk());
    }

    // ================= GET /products/search/{keyword} =================

    @Test
    void searchProduct_success() throws Exception {
        when(productService.searchProduct("air"))
                .thenReturn(List.of(ProductResponse.builder().productId("1").build()));

        mockMvc.perform(get("/products/search/air"))
                .andExpect(status().isOk());
    }

    // ================= POST /products/{id}/images/upload =================

    @Test
    @WithMockUser
    void uploadImages_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "img.png", "image/png", "test".getBytes()
        );

        when(productService.uploadImages(any(), any(), any(), any()))
                .thenReturn(List.of(
                        ProductImageResponse.builder()
                                .imageUrl("url")
                                .imageType("MAIN")
                                .build()
                ));

        mockMvc.perform(multipart("/products/p1/images/upload")
                        .file(file)
                        .param("imageType", "MAIN")
                        .param("description", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));
    }
}
