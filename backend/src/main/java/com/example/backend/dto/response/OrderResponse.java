package com.example.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    String orderId;
    double totalAmount;
    String status; // NEW
    java.time.LocalDateTime createdAt; // NEW
    ShipmentResponse shipment; // NEW
    boolean cancellationRequested; // NEW
    List<OrderItemResponse> items;
}
