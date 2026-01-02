package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.dto.request.AddressDTO;
import com.example.backend.dto.request.ShopCreationRequest;
import com.example.backend.dto.response.ShopResponse;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.service.ShopService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShopController.class)
@Import(SecurityConfig.class)
class ShopControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ShopService shopService;

    @MockBean
    CustomJwtDecoder customJwtDecoder; // để SecurityConfig không lỗi

    // ================= POST /shops =================

    @Test
    @WithMockUser(username = "user1")
    void createShop_success() throws Exception {
        ShopCreationRequest request = ShopCreationRequest.builder()
                .name("My Shop")
                .address(AddressDTO.builder()
                        .name("Huy")
                        .phone("0123456789")
                        .addressDetail("HN")
                        .wardCode("001")
                        .build())
                .build();

        when(shopService.createShop(any()))
                .thenReturn(ShopResponse.builder()
                        .shopId("shop1")
                        .name("My Shop")
                        .ownerUsername("user1")
                        .build());

        mockMvc.perform(post("/shops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.shopId").value("shop1"))
                .andExpect(jsonPath("$.result.name").value("My Shop"));
    }

    @Test
    @WithMockUser(username = "user1")
    void createShop_userNotExist() throws Exception {
        ShopCreationRequest request = new ShopCreationRequest();

        when(shopService.createShop(any()))
                .thenThrow(new AppException(ErrorCode.USER_NOT_EXIST));

        mockMvc.perform(post("/shops")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(ErrorCode.USER_NOT_EXIST.getMessage()));
    }

    // ================= GET /shops =================

    @Test
    void getAllShops_success() throws Exception {
        when(shopService.getAllShop())
                .thenReturn(List.of(
                        ShopResponse.builder().shopId("1").build(),
                        ShopResponse.builder().shopId("2").build()
                ));

        mockMvc.perform(get("/shops"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2));
    }

    // ================= GET /shops/{id} =================

    @Test
    void getShopById_success() throws Exception {
        when(shopService.getShopById("shop1"))
                .thenReturn(ShopResponse.builder()
                        .shopId("shop1")
                        .name("Shop A")
                        .build());

        mockMvc.perform(get("/shops/shop1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shopId").value("shop1"));
    }

    @Test
    void getShopById_notExist() throws Exception {
        when(shopService.getShopById("shopX"))
                .thenThrow(new AppException(ErrorCode.SHOP_NOT_EXIST));

        mockMvc.perform(get("/shops/shopX"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(ErrorCode.SHOP_NOT_EXIST.getMessage()));
    }

    // ================= GET /shops/owner/{username} =================

    @Test
    void getShopByOwner_success() throws Exception {
        when(shopService.getAllShopByOwnerId("user1"))
                .thenReturn(List.of(
                        ShopResponse.builder().shopId("1").build(),
                        ShopResponse.builder().shopId("2").build()
                ));

        mockMvc.perform(get("/shops/owner/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2));
    }

    @Test
    void getShopByOwner_userNotExist() throws Exception {
        when(shopService.getAllShopByOwnerId("unknown"))
                .thenThrow(new AppException(ErrorCode.USER_NOT_EXIST));

        mockMvc.perform(get("/shops/owner/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(ErrorCode.USER_NOT_EXIST.getMessage()));
    }

    // ================= GET /shops/search =================

    @Test
    void searchShop_districtAndProvince() throws Exception {
        when(shopService.getShopsByDistrictAndProvince("D1", "HN"))
                .thenReturn(List.of(ShopResponse.builder().shopId("1").build(),
                        ShopResponse.builder().shopId("2").build(),
                        ShopResponse.builder().shopId("3").build(),
                        ShopResponse.builder().shopId("4").build()
                ));

        mockMvc.perform(get("/shops/search")
                        .param("district", "D1")
                        .param("province", "HN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(4));
    }

    @Test
    void searchShop_districtOnly() throws Exception {
        when(shopService.getShopsByDistrict("D1"))
                .thenReturn(List.of(ShopResponse.builder().shopId("1").build()));

        mockMvc.perform(get("/shops/search")
                        .param("district", "D1"))
                .andExpect(status().isOk());
    }

    @Test
    void searchShop_provinceOnly() throws Exception {
        when(shopService.getShopsByProvince("HN"))
                .thenReturn(List.of(ShopResponse.builder().shopId("1").build()));

        mockMvc.perform(get("/shops/search")
                        .param("province", "HN"))
                .andExpect(status().isOk());
    }

    @Test
    void searchShop_noParam() throws Exception {
        when(shopService.getAllShop())
                .thenReturn(List.of(ShopResponse.builder().shopId("1").build()));

        mockMvc.perform(get("/shops/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1));
    }
}

