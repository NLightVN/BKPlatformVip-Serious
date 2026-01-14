package com.example.backend.controller;

import com.example.backend.dto.request.BanRequest;
import com.example.backend.dto.response.*;
import com.example.backend.service.OrderService;
import com.example.backend.service.ProductService;
import com.example.backend.service.ShopService;
import com.example.backend.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    UserService userService;
    ShopService shopService;
    ProductService productService;
    OrderService orderService;

    // ========== User Management ==========
    
    @GetMapping("/users")
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .result(userService.getAllUser())
                .build();
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<UserResponse> getUserById(@PathVariable String userId) {
        return ApiResponse.<UserResponse>builder()
                .result(userService.getUserById(userId))
                .build();
    }

    @PutMapping("/users/{userId}/ban")
    public ApiResponse<UserResponse> toggleBanUser(
            @PathVariable String userId,
            @RequestBody BanRequest request) {
        UserResponse result = request.isBanned() 
                ? userService.banUser(userId) 
                : userService.unbanUser(userId);
        return ApiResponse.<UserResponse>builder()
                .result(result)
                .build();
    }

    // ========== Shop Management ==========

    @GetMapping("/shops/{shopId}")
    public ApiResponse<ShopResponse> getShopById(@PathVariable String shopId) {
        return ApiResponse.<ShopResponse>builder()
                .result(shopService.getShopById(shopId))
                .build();
    }

    @PutMapping("/shops/{shopId}/ban")
    public ApiResponse<ShopResponse> toggleBanShop(
            @PathVariable String shopId,
            @RequestBody BanRequest request) {
        ShopResponse result = request.isBanned() 
                ? shopService.banShop(shopId) 
                : shopService.unbanShop(shopId);
        return ApiResponse.<ShopResponse>builder()
                .result(result)
                .build();
    }

    // ========== Product Management ==========

    @GetMapping("/products")
    public ApiResponse<List<ProductResponse>> getAllProducts() {
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productService.getAllProductsGlobal())
                .build();
    }

    @PutMapping("/products/{productId}/ban")
    public ApiResponse<ProductResponse> toggleBanProduct(
            @PathVariable String productId,
            @RequestBody BanRequest request) {
        ProductResponse result = request.isBanned() 
                ? productService.banProduct(productId) 
                : productService.unbanProduct(productId);
        return ApiResponse.<ProductResponse>builder()
                .result(result)
                .build();
    }

    // ========== Order Management ==========

    @GetMapping("/orders/{orderId}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable String orderId) {
        return ApiResponse.<OrderResponse>builder()
                .result(orderService.getOrderById(orderId))
                .build();
    }
}
