package com.example.backend.controller;

import com.example.backend.dto.request.OrderSelectedItemsRequest;
import com.example.backend.dto.request.OrderStatusUpdateRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.OrderResponse;
import com.example.backend.enums.OrderStatus;
import com.example.backend.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderController {

    OrderService orderService;

    // Tạo order từ cart của user
    @PostMapping("/checkout/selected")
    public ApiResponse<List<OrderResponse>> checkoutSelected(
            @RequestBody OrderSelectedItemsRequest request) {
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orderService.checkoutSelectedItems(request))
                .build();
    }
    //OK

    // Lấy tất cả order của user
    @GetMapping("/user/{userId}")
    public ApiResponse<List<OrderResponse>> getOrdersByUser(@PathVariable String userId) {
        List<OrderResponse> orders = orderService.getOrdersByUser(userId);
        return ApiResponse.<List<OrderResponse>>builder()
                .result(orders)
                .build();
    }
    //OK

    // Lấy order theo id
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable String orderId) {
        OrderResponse order = orderService.getOrderById(orderId);
        return ApiResponse.<OrderResponse>builder()
                .result(order)
                .build();
    }
    //OK

    // Cập nhật trạng thái order
    @PatchMapping("/{orderId}/status")
    public ApiResponse<OrderResponse> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody OrderStatusUpdateRequest request
    ) {
        OrderResponse updatedOrder = orderService.updateOrderStatus(orderId, request.getStatus());
        return ApiResponse.<OrderResponse>builder()
                .result(updatedOrder)
                .build();
    }
}
