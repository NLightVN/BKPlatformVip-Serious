package com.example.backend.service;

import com.example.backend.dto.response.DistrictResponse;
import com.example.backend.dto.response.ProvinceResponse;
import com.example.backend.dto.response.WardResponse;
import com.example.backend.repository.DistrictRepository;
import com.example.backend.repository.ProvinceRepository;
import com.example.backend.repository.WardRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LocationService {

    ProvinceRepository provinceRepository;
    DistrictRepository districtRepository;
    WardRepository wardRepository;

    public List<ProvinceResponse> getAllProvinces() {
        return provinceRepository.findAll().stream()
                .map(p -> ProvinceResponse.builder()
                        .code(p.getCode())
                        .fullName(p.getFullName())
                        .build())
                .toList();
    }

    public List<DistrictResponse> getDistrictsByProvince(String provinceCode) {
        return districtRepository.findAllByProvince_Code(provinceCode).stream()
                .map(d -> DistrictResponse.builder()
                        .code(d.getCode())
                        .fullName(d.getFullName())
                        .build())
                .toList();
    }

    public List<WardResponse> getWardsByDistrict(String districtCode) {
        return wardRepository.findAllByDistrict_Code(districtCode).stream()
                .map(w -> WardResponse.builder()
                        .code(w.getCode())
                        .fullName(w.getFullName())
                        .build())
                .toList();
    }
}
