package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.dto.request.OrderSelectedItemsRequest;
import com.example.backend.dto.request.OrderStatusUpdateRequest;
import com.example.backend.dto.response.OrderResponse;
import com.example.backend.enums.OrderStatus;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.OrderService;
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
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @MockitoBean
    OrderService orderService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user1")
    void checkoutSelectedItems_success() throws Exception {
        OrderSelectedItemsRequest request = new OrderSelectedItemsRequest();
        request.setProductIds(List.of("product-1", "product-2"));

        OrderResponse order1 = OrderResponse.builder()
                .orderId("order-1")
                .totalAmount(200000)
                .build();
        OrderResponse order2 = OrderResponse.builder()
                .orderId("order-2")
                .totalAmount(150000)
                .build();

        when(orderService.checkoutSelectedItems(any()))
                .thenReturn(List.of(order1, order2));

        mockMvc.perform(post("/orders/checkout/selected")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2));
    }

    @Test
    @WithMockUser(username = "user1")
    void checkoutSelectedItems_cartEmpty() throws Exception {
        OrderSelectedItemsRequest request = new OrderSelectedItemsRequest();
        request.setProductIds(List.of("product-1"));

        when(orderService.checkoutSelectedItems(any()))
                .thenThrow(new AppException(ErrorCode.CART_EMPTY));

        mockMvc.perform(post("/orders/checkout/selected")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user1")
    void getOrdersByUser_success() throws Exception {
        OrderResponse order1 = OrderResponse.builder()
                .orderId("order-1")
                .totalAmount(200000)
                .build();
        OrderResponse order2 = OrderResponse.builder()
                .orderId("order-2")
                .totalAmount(150000)
                .build();

        when(orderService.getOrdersByUser("user-1"))
                .thenReturn(List.of(order1, order2));

        mockMvc.perform(get("/orders/user/user-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2));
    }

    @Test
    @WithMockUser(username = "user1")
    void getOrderById_success() throws Exception {
        OrderResponse response = OrderResponse.builder()
                .orderId("order-1")
                .totalAmount(200000)
                .build();

        when(orderService.getOrderById("order-1"))
                .thenReturn(response);

        mockMvc.perform(get("/orders/order-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.orderId").value("order-1"));
    }

    @Test
    @WithMockUser(username = "user1")
    void getOrderById_notFound() throws Exception {
        when(orderService.getOrderById("non-existent"))
                .thenThrow(new AppException(ErrorCode.ORDER_NOT_EXIST));

        mockMvc.perform(get("/orders/non-existent"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_NOT_EXIST.getCode()));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateOrderStatus_success() throws Exception {
        OrderStatusUpdateRequest request = OrderStatusUpdateRequest.builder()
                .status(OrderStatus.DELIVERED)
                .build();

        OrderResponse response = OrderResponse.builder()
                .orderId("order-1")
                .totalAmount(200000)
                .build();

        when(orderService.updateOrderStatus(eq("order-1"), eq(OrderStatus.DELIVERED)))
                .thenReturn(response);

        mockMvc.perform(put("/orders/order-1/status")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.orderId").value("order-1"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void updateOrderStatus_forbidden() throws Exception {
        // giả lập service ném AppException nếu user không phải admin
        doThrow(new AppException(ErrorCode.UNAUTHORIZED))
                .when(orderService)
                .updateOrderStatus(any(), any());

        OrderStatusUpdateRequest request = OrderStatusUpdateRequest.builder()
                .status(OrderStatus.DELIVERED)
                .build();

        mockMvc.perform(put("/orders/order-1/status")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()) // status map từ ErrorCode.UNAUTHORIZED
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.message").value(ErrorCode.UNAUTHORIZED.getMessage()));
    }
}

