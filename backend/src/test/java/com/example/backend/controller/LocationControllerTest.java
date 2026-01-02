package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.dto.response.DistrictResponse;
import com.example.backend.dto.response.ProvinceResponse;
import com.example.backend.dto.response.WardResponse;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.LocationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LocationController.class)
@Import(SecurityConfig.class)
class LocationControllerTest {

    @Autowired
    MockMvc mockMvc;

    // Mock để SecurityConfig không lỗi
    @MockitoBean
    CustomJwtDecoder customJwtDecoder;

    // Controller dùng Service → mock Service
    @MockitoBean
    LocationService locationService;

    // ================= GET /locations/provinces =================

    @Test
    void getAllProvinces_success() throws Exception {
        when(locationService.getAllProvinces())
                .thenReturn(List.of(
                        ProvinceResponse.builder()
                                .code("01")
                                .fullName("Thành phố Hà Nội")
                                .build(),
                        ProvinceResponse.builder()
                                .code("79")
                                .fullName("Thành phố Hồ Chí Minh")
                                .build()
                ));

        mockMvc.perform(get("/locations/provinces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].code").value("01"))
                .andExpect(jsonPath("$.result[0].fullName").value("Thành phố Hà Nội"));
    }

    // ================= GET /locations/districts/{provinceCode} =================

    @Test
    void getDistrictsByProvince_success() throws Exception {
        when(locationService.getDistrictsByProvince("01"))
                .thenReturn(List.of(
                        DistrictResponse.builder()
                                .code("001")
                                .fullName("Quận Ba Đình")
                                .build(),
                        DistrictResponse.builder()
                                .code("002")
                                .fullName("Quận Hoàn Kiếm")
                                .build()
                ));

        mockMvc.perform(get("/locations/districts/01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].code").value("001"))
                .andExpect(jsonPath("$.result[0].fullName").value("Quận Ba Đình"));
    }

    // ================= GET /locations/wards/{districtCode} =================

    @Test
    void getWardsByDistrict_success() throws Exception {
        when(locationService.getWardsByDistrict("001"))
                .thenReturn(List.of(
                        WardResponse.builder()
                                .code("00001")
                                .fullName("Phường Phúc Xá")
                                .build(),
                        WardResponse.builder()
                                .code("00004")
                                .fullName("Phường Trúc Bạch")
                                .build()
                ));

        mockMvc.perform(get("/locations/wards/001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].code").value("00001"))
                .andExpect(jsonPath("$.result[0].fullName").value("Phường Phúc Xá"));
    }
}
