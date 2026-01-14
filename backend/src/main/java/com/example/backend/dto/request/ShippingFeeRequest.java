package com.example.backend.dto.request;

import lombok.Data;

@Data
public class ShippingFeeRequest {
    String fromDistrictCode;
    String toDistrictCode;
    String fromWardCode;
    String toWardCode;
    int weightGram;
}