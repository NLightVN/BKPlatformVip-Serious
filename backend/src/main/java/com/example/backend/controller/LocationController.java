package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.DistrictResponse;
import com.example.backend.dto.response.ProvinceResponse;
import com.example.backend.dto.response.WardResponse;
import com.example.backend.repository.DistrictRepository;
import com.example.backend.repository.ProvinceRepository;
import com.example.backend.repository.WardRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/locations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationController {

    ProvinceRepository provinceRepository;
    DistrictRepository districtRepository;
    WardRepository wardRepository;

    @GetMapping("/provinces")
    public ApiResponse<List<ProvinceResponse>> getAllProvinces() {
        return ApiResponse.<List<ProvinceResponse>>builder()
                .result(
                        provinceRepository.findAll().stream()
                                .map(p -> ProvinceResponse.builder()
                                        .code(p.getCode())
                                        .fullName(p.getFullName())
                                        .build())
                                .toList()
                )
                .build();
    }

    @GetMapping("/districts/{provinceCode}")
    public ApiResponse<List<DistrictResponse>> getDistrictsByProvince(
            @PathVariable String provinceCode
    ) {
        return ApiResponse.<List<DistrictResponse>>builder()
                .result(
                        districtRepository.findAllByProvince_Code(provinceCode).stream()
                                .map(d -> DistrictResponse.builder()
                                        .code(d.getCode())
                                        .fullName(d.getFullName())
                                        .build())
                                .toList()
                )
                .build();
    }

    @GetMapping("/wards/{districtCode}")
    public ApiResponse<List<WardResponse>> getWardsByDistrict(
            @PathVariable String districtCode
    ) {
        return ApiResponse.<List<WardResponse>>builder()
                .result(
                        wardRepository.findAllByDistrict_Code(districtCode).stream()
                                .map(w -> WardResponse.builder()
                                        .code(w.getCode())
                                        .fullName(w.getFullName())
                                        .build())
                                .toList()
                )
                .build();
    }
}
