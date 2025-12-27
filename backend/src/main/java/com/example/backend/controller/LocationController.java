package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.entity.District;
import com.example.backend.entity.Province;
import com.example.backend.entity.Ward;
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
    public ApiResponse<List<Province>> getAllProvinces() {
        return ApiResponse.<List<Province>>builder()
                .result(provinceRepository.findAll())
                .build();
    }

    @GetMapping("/districts/{provinceCode}")
    public ApiResponse<List<District>> getDistrictsByProvince(@PathVariable String provinceCode) {
        return ApiResponse.<List<District>>builder()
                .result(districtRepository.findAllByProvince_Code(provinceCode))
                .build();
    }

    @GetMapping("/wards/{districtCode}")
    public ApiResponse<List<Ward>> getWardsByDistrict(@PathVariable String districtCode) {
        return ApiResponse.<List<Ward>>builder()
                .result(wardRepository.findAllByDistrict_Code(districtCode))
                .build();
    }
}