package com.example.backend.service;

import com.example.backend.dto.request.AddressDTO;
import com.example.backend.dto.request.ShopCreationRequest;
import com.example.backend.dto.response.ShopResponse;
import com.example.backend.entity.Shop;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.mapper.ShopMapper;
import com.example.backend.repository.ShopRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    ShopRepository shopRepository;

    @Mock
    ShopMapper shopMapper;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    ShopService shopService;

    private User testUser;
    private Shop testShop;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId("user-1")
                .username("testuser")
                .email("test@sis.hust.edu.vn")
                .build();

        testShop = Shop.builder()
                .shopId("shop-1")
                .name("Test Shop")
                .owner(testUser)
                .build();
    }

    @Test
    @SuppressWarnings("null")
    void createShop_success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");

            AddressDTO address = AddressDTO.builder()
                    .name("Test Shop")
                    .phone("0123456789")
                    .addressDetail("123 Test Street")
                    .wardCode("001")
                    .build();

            ShopCreationRequest request = ShopCreationRequest.builder()
                    .name("My Shop")
                    .address(address)
                    .build();

            Shop newShop = Shop.builder()
                    .name("My Shop")
                    .build();

            ShopResponse response = ShopResponse.builder()
                    .shopId("shop-2")
                    .name("My Shop")
                    .ownerUsername("testuser")
                    .build();

            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(shopMapper.toShop(request)).thenReturn(newShop);
            when(shopRepository.save(any(Shop.class))).thenAnswer(invocation -> {
                Shop shop = Objects.requireNonNull(invocation.getArgument(0, Shop.class));
                shop.setShopId("shop-2");
                return shop;
            });
            when(shopMapper.toShopResponse(any(Shop.class))).thenReturn(response);

            ShopResponse result = shopService.createShop(request);

            assertNotNull(result);
            assertEquals("My Shop", result.getName());
            verify(shopRepository).save(any(Shop.class));
        }
    }

    @Test
    void createShop_userNotExist() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("non-existent");

            ShopCreationRequest request = ShopCreationRequest.builder()
                    .name("My Shop")
                    .build();

            when(userRepository.findByUsername("non-existent")).thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class, () -> {
                shopService.createShop(request);
            });

            assertEquals(ErrorCode.USER_NOT_EXIST, exception.getErrorCode());
        }
    }

    @Test
    void getAllShop_success() {
        Shop shop2 = Shop.builder()
                .shopId("shop-2")
                .name("Shop 2")
                .build();

        ShopResponse response1 = ShopResponse.builder()
                .shopId("shop-1")
                .name("Test Shop")
                .build();
        ShopResponse response2 = ShopResponse.builder()
                .shopId("shop-2")
                .name("Shop 2")
                .build();

        when(shopRepository.findAll()).thenReturn(List.of(testShop, shop2));
        when(shopMapper.toShopResponse(testShop)).thenReturn(response1);
        when(shopMapper.toShopResponse(shop2)).thenReturn(response2);

        List<ShopResponse> results = shopService.getAllShop();

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void getShopById_success() {
        ShopResponse response = ShopResponse.builder()
                .shopId("shop-1")
                .name("Test Shop")
                .build();

        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(shopMapper.toShopResponse(testShop)).thenReturn(response);

        ShopResponse result = shopService.getShopById("shop-1");

        assertNotNull(result);
        assertEquals("shop-1", result.getShopId());
    }

    @Test
    void getShopById_notFound() {
        when(shopRepository.findById("non-existent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            shopService.getShopById("non-existent");
        });

        assertEquals(ErrorCode.SHOP_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    void getAllShopByOwnerId_success() {
        ShopResponse response = ShopResponse.builder()
                .shopId("shop-1")
                .name("Test Shop")
                .ownerUsername("testuser")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(shopRepository.findAllByOwner(testUser)).thenReturn(List.of(testShop));
        when(shopMapper.toShopResponse(testShop)).thenReturn(response);

        List<ShopResponse> results = shopService.getAllShopByOwnerId("testuser");

        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("testuser", results.get(0).getOwnerUsername());
    }

    // ================= BAN SHOP =================

    @Test
    void banShop_success() {
        ShopResponse response = ShopResponse.builder()
                .shopId("shop-1")
                .name("Test Shop")
                .status("BANNED")
                .build();

        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(shopRepository.save(any())).thenReturn(testShop);
        when(shopMapper.toShopResponse(any())).thenReturn(response);

        ShopResponse result = shopService.banShop("shop-1");

        assertNotNull(result);
        assertEquals("BANNED", result.getStatus());
        verify(shopRepository).save(any());
    }

    @Test
    void banShop_shopNotExist() {
        when(shopRepository.findById("non-existent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            shopService.banShop("non-existent");
        });

        assertEquals(ErrorCode.SHOP_NOT_EXIST, exception.getErrorCode());
    }

    // ================= UNBAN SHOP =================

    @Test
    void unbanShop_success() {
        ShopResponse response = ShopResponse.builder()
                .shopId("shop-1")
                .name("Test Shop")
                .status("ACTIVE")
                .build();

        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(shopRepository.save(any())).thenReturn(testShop);
        when(shopMapper.toShopResponse(any())).thenReturn(response);

        ShopResponse result = shopService.unbanShop("shop-1");

        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
        verify(shopRepository).save(any());
    }

    @Test
    void unbanShop_shopNotExist() {
        when(shopRepository.findById("non-existent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            shopService.unbanShop("non-existent");
        });

        assertEquals(ErrorCode.SHOP_NOT_EXIST, exception.getErrorCode());
    }
}
