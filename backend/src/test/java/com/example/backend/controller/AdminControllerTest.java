package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.dto.request.BanRequest;
import com.example.backend.dto.response.*;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CustomJwtDecoder customJwtDecoder;

    @MockitoBean
    UserService userService;

    @MockitoBean
    ShopService shopService;

    @MockitoBean
    ProductService productService;

    @MockitoBean
    OrderService orderService;

    // ================= GET ALL USERS =================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllUsers_success() throws Exception {
        // mock service trả về list users
        when(userService.getAllUser()).thenReturn(List.of(
                UserResponse.builder()
                        .userId("1")
                        .username("user1")
                        .status("ACTIVE")
                        .build(),
                UserResponse.builder()
                        .userId("2")
                        .username("user2")
                        .status("BANNED")
                        .build()
        ));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].username").value("user1"))
                .andExpect(jsonPath("$.result[1].status").value("BANNED"));
    }

    @Test
    @WithMockUser(username = "regularuser", roles = {"USER"})
    void getAllUsers_nonAdmin() throws Exception {
        // regular user không có quyền truy cập admin endpoint
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden()); // 403
    }

    // ================= GET USER BY ID =================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getUserById_success() throws Exception {
        when(userService.getUserById("user-123")).thenReturn(
                UserResponse.builder()
                        .userId("user-123")
                        .username("testuser")
                        .email("test@sis.hust.edu.vn")
                        .build()
        );

        mockMvc.perform(get("/admin/users/user-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.userId").value("user-123"))
                .andExpect(jsonPath("$.result.username").value("testuser"));
    }

    // ================= BAN/UNBAN USER =================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void toggleBanUser_banSuccess() throws Exception {
        BanRequest request = new BanRequest();
        request.setBanned(true); // ban user

        when(userService.banUser("user-123")).thenReturn(
                UserResponse.builder()
                        .userId("user-123")
                        .username("testuser")
                        .status("BANNED")
                        .build()
        );

        mockMvc.perform(put("/admin/users/user-123/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("BANNED"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void toggleBanUser_unbanSuccess() throws Exception {
        BanRequest request = new BanRequest();
        request.setBanned(false); // unban user

        when(userService.unbanUser("user-123")).thenReturn(
                UserResponse.builder()
                        .userId("user-123")
                        .username("testuser")
                        .status("ACTIVE")
                        .build()
        );

        mockMvc.perform(put("/admin/users/user-123/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "regularuser", roles = {"USER"})
    void toggleBanUser_nonAdmin() throws Exception {
        BanRequest request = new BanRequest();
        request.setBanned(true);

        // regular user không có quyền ban/unban
        mockMvc.perform(put("/admin/users/user-123/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden()); // 403
    }

    // ================= GET SHOP BY ID =================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getShopById_success() throws Exception {
        when(shopService.getShopById("shop-123")).thenReturn(
                ShopResponse.builder()
                        .shopId("shop-123")
                        .name("Test Shop")
                        .status("ACTIVE")
                        .build()
        );

        mockMvc.perform(get("/admin/shops/shop-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.shopId").value("shop-123"))
                .andExpect(jsonPath("$.result.name").value("Test Shop"));
    }

    // ================= BAN/UNBAN SHOP =================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void toggleBanShop_banSuccess() throws Exception {
        BanRequest request = new BanRequest();
        request.setBanned(true);

        when(shopService.banShop("shop-123")).thenReturn(
                ShopResponse.builder()
                        .shopId("shop-123")
                        .name("Test Shop")
                        .status("BANNED")
                        .build()
        );

        mockMvc.perform(put("/admin/shops/shop-123/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("BANNED"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void toggleBanShop_unbanSuccess() throws Exception {
        BanRequest request = new BanRequest();
        request.setBanned(false);

        when(shopService.unbanShop("shop-123")).thenReturn(
                ShopResponse.builder()
                        .shopId("shop-123")
                        .name("Test Shop")
                        .status("ACTIVE")
                        .build()
        );

        mockMvc.perform(put("/admin/shops/shop-123/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("ACTIVE"));
    }

    // ================= GET ALL PRODUCTS (ADMIN) =================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllProducts_success() throws Exception {
        // admin có thể xem tất cả products kể cả banned
        when(productService.getAllProductsGlobal()).thenReturn(List.of(
                ProductResponse.builder()
                        .productId("p1")
                        .name("Product 1")
                        .status("ACTIVE")
                        .build(),
                ProductResponse.builder()
                        .productId("p2")
                        .name("Product 2")
                        .status("BANNED")
                        .build()
        ));

        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(2))
                .andExpect(jsonPath("$.result[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.result[1].status").value("BANNED"));
    }

    // ================= BAN/UNBAN PRODUCT =================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void toggleBanProduct_banSuccess() throws Exception {
        BanRequest request = new BanRequest();
        request.setBanned(true);

        when(productService.banProduct("prod-123")).thenReturn(
                ProductResponse.builder()
                        .productId("prod-123")
                        .name("Test Product")
                        .status("BANNED")
                        .build()
        );

        mockMvc.perform(put("/admin/products/prod-123/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("BANNED"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void toggleBanProduct_unbanSuccess() throws Exception {
        BanRequest request = new BanRequest();
        request.setBanned(false);

        when(productService.unbanProduct("prod-123")).thenReturn(
                ProductResponse.builder()
                        .productId("prod-123")
                        .name("Test Product")
                        .status("ACTIVE")
                        .build()
        );

        mockMvc.perform(put("/admin/products/prod-123/ban")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.status").value("ACTIVE"));
    }

    // ================= GET ORDER BY ID (ADMIN) =================

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getOrderById_success() throws Exception {
        when(orderService.getOrderById("order-123")).thenReturn(
                OrderResponse.builder()
                        .orderId("order-123")
                        .totalAmount(100000.0)
                        .build()
        );

        mockMvc.perform(get("/admin/orders/order-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.orderId").value("order-123"))
                .andExpect(jsonPath("$.result.totalAmount").value(100000.0));
    }
}
