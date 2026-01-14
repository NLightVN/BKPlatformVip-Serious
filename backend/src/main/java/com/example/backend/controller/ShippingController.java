package com.example.backend.controller;

import com.example.backend.dto.request.ShippingFeeRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.ShippingFeeResponse;
import com.example.backend.service.ShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shipping")
@RequiredArgsConstructor
public class ShippingController {
    private final ShippingService shippingService;

    @PostMapping("/calculate")
    public ApiResponse<ShippingFeeResponse> calculateFee(@RequestBody ShippingFeeRequest request) {
        return ApiResponse.<ShippingFeeResponse>builder()
                .result(shippingService.calculateShippingFee(request))
                .build();
    }
}