package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    String orderId;
    String transactionId;
    String status;
    double amount;
    String paymentMethod;
    LocalDateTime paymentDate;
    String message;
}