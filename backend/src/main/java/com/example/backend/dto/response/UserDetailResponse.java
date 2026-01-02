package com.example.backend.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDetailResponse {
    String userId;
    String username;
    String fullname;
    String email;
    String status;
    
    // Order history
    List<OrderResponse> recentOrders;
    
    // Recent activities (simplified)
    List<String> recentActivities;
}
