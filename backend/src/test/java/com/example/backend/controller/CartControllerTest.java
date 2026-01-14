package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.dto.request.CartRequest;
import com.example.backend.dto.response.CartResponse;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
class CartControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @MockitoBean
    CartService cartService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user1")
    void addToCart_success() throws Exception {
        CartRequest request = CartRequest.builder()
                .productId("product-1")
                .quantity(2)
                .build();

        CartResponse response = CartResponse.builder()
                .id("cart-1")
                .totalAmount(200000)
                .build();

        when(cartService.addToCart(any(), eq("user-1")))
                .thenReturn(response);

        mockMvc.perform(post("/cart/add/user-1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value("cart-1"))
                .andExpect(jsonPath("$.result.totalAmount").value(200000));
    }

    @Test
    @WithMockUser(username = "user1")
    void addToCart_invalidQuantity() throws Exception {
        CartRequest request = CartRequest.builder()
                .productId("product-1")
                .quantity(0)
                .build();

        when(cartService.addToCart(any(), eq("user-1")))
                .thenThrow(new AppException(ErrorCode.INVALID_VALUE));

        mockMvc.perform(post("/cart/add/user-1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user1")
    void removeFromCart_success() throws Exception {
        CartResponse response = CartResponse.builder()
                .id("cart-1")
                .totalAmount(100000)
                .build();

        when(cartService.removeFromCart("user-1", "product-1"))
                .thenReturn(response);

        mockMvc.perform(delete("/cart/remove/user-1/product-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value("cart-1"));
    }

    @Test
    @WithMockUser(username = "user1")
    void removeFromCart_itemNotExist() throws Exception {
        when(cartService.removeFromCart("user-1", "non-existent"))
                .thenThrow(new AppException(ErrorCode.CART_ITEM_NOT_EXIST));

        mockMvc.perform(delete("/cart/remove/user-1/non-existent"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user1")
    void clearCart_success() throws Exception {
        CartResponse response = CartResponse.builder()
                .id("cart-1")
                .build();

        when(cartService.clearCart("user-1"))
                .thenReturn(response);

        mockMvc.perform(delete("/cart/clear/user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalAmount").value(0));
    }

    @Test
    @WithMockUser(username = "user1")
    void getCartByUser_success() throws Exception {
        CartResponse response = CartResponse.builder()
                .id("cart-1")
                .totalAmount(150000)
                .build();

        when(cartService.getCartByUser("user-1"))
                .thenReturn(response);

        mockMvc.perform(get("/cart/user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value("cart-1"));
    }
}