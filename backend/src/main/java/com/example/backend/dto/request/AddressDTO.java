package com.example.backend.dto.request; // Hoặc response tùy package bạn để

import com.example.backend.dto.response.DistrictResponse;
import com.example.backend.dto.response.ProvinceResponse;
import com.example.backend.dto.response.WardResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    String phone;
    String name;
    String addressDetail;
    String wardCode;
    WardResponse ward;          // Chứa fullName phường
    DistrictResponse district;  // Chứa fullName quận
    ProvinceResponse province;  // Chứa fullName tỉnh
}