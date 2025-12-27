package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShippingFeeResponse {
    double fee;
    int estimatedDays;
    String provider;
}