package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.DistrictResponse;
import com.example.backend.dto.response.ProvinceResponse;
import com.example.backend.dto.response.WardResponse;
import com.example.backend.service.LocationService;
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

    LocationService locationService;

    @GetMapping("/provinces")
    public ApiResponse<List<ProvinceResponse>> getAllProvinces() {
        return ApiResponse.<List<ProvinceResponse>>builder()
                .result(locationService.getAllProvinces())
                .build();
    }

    @GetMapping("/districts/{provinceCode}")
    public ApiResponse<List<DistrictResponse>> getDistrictsByProvince(
            @PathVariable String provinceCode
    ) {
        return ApiResponse.<List<DistrictResponse>>builder()
                .result(locationService.getDistrictsByProvince(provinceCode))
                .build();
    }

    @GetMapping("/wards/{districtCode}")
    public ApiResponse<List<WardResponse>> getWardsByDistrict(
            @PathVariable String districtCode
    ) {
        return ApiResponse.<List<WardResponse>>builder()
                .result(locationService.getWardsByDistrict(districtCode))
                .build();
    }
}
