package com.example.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    private String productId;
    private String productName;
    private int quantity;
    private double priceAtPurchase;
    private String productStatus; // Track product status (ACTIVE, DELETED, BANNED)
}
