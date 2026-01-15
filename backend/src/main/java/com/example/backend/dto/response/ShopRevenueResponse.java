package com.example.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopRevenueResponse {
    double totalRevenue;
    long totalOrders;
    long pendingOrders;
    long awaitingPickupOrders;
    long shippedOrders;
    long deliveredOrders;
    long cancelledOrders;
    double averageOrderValue;
}
