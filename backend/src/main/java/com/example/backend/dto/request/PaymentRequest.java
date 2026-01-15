package com.example.backend.dto.request;

import lombok.Data;

@Data
public class PaymentRequest {
    String orderId;
    String paymentMethod; // VNPAY, MOMO, COD
}