package com.example.backend.controller;

import com.example.backend.dto.request.PaymentRequest;
import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.PaymentResponse;
import com.example.backend.service.PaymentSimulationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentController {

    PaymentSimulationService paymentService;

    /**
     * Giả lập thanh toán online (VNPay, Momo)
     * POST /payments/online
     */
    @PostMapping("/online")
    public ApiResponse<PaymentResponse> payOnline(@RequestBody PaymentRequest request) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.simulateOnlinePayment(request))
                .build();
    }

    /**
     * Tạo đơn COD
     * POST /payments/cod/{orderId}
     */
    @PostMapping("/cod/{orderId}")
    public ApiResponse<PaymentResponse> createCOD(@PathVariable String orderId) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.createCODPayment(orderId))
                .build();
    }

    /**
     * Admin xác nhận đã nhận tiền COD
     * POST /payments/cod/confirm/{orderId}
     */
    @PostMapping("/cod/confirm/{orderId}")
    public ApiResponse<PaymentResponse> confirmCOD(@PathVariable String orderId) {
        return ApiResponse.<PaymentResponse>builder()
                .result(paymentService.confirmCODPayment(orderId))
                .build();
    }
}