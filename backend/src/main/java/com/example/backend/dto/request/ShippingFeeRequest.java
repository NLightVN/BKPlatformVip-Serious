package com.example.backend.dto.request;

import lombok.Data;

@Data
public class ShippingFeeRequest {
    String fromDistrictCode;
    String toDistrictCode;
    String toWardCode;
    int weightGram;
}