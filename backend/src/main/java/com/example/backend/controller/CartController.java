package com.example.backend.controller;

import com.example.backend.dto.request.CartRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.CartResponse;
import com.example.backend.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartController {

    CartService cartService;

    @PostMapping("/add/{userId}")
    public ApiResponse<CartResponse> addToCart(@PathVariable String userId,
                                               @RequestBody CartRequest request) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.addToCart(request, userId))
                .build();
    }

    @DeleteMapping("/remove/{userId}/{cartItemId}")
    public ApiResponse<CartResponse> removeFromCart(@PathVariable String userId,
                                                    @PathVariable String cartItemId) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.removeFromCart(userId, cartItemId))
                .build();
    }

    @DeleteMapping("/clear/{userId}")
    public ApiResponse<CartResponse> clearCart(@PathVariable String userId) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.clearCart(userId))
                .build();
    }

    @GetMapping("/{userId}")
    public ApiResponse<CartResponse> getCartByUser(@PathVariable String userId) {
        return ApiResponse.<CartResponse>builder()
                .result(cartService.getCartByUser(userId))
                .build();
    }
}
