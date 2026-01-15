package com.example.backend.dto.request;

import com.example.backend.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderStatusUpdateRequest {
    private OrderStatus status;
}
