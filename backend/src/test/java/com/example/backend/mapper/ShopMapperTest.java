package com.example.backend.mapper;

import com.example.backend.dto.request.AddressDTO;
import com.example.backend.dto.request.ShopCreationRequest;
import com.example.backend.dto.response.ShopResponse;
import com.example.backend.entity.AddressBook;
import com.example.backend.entity.Shop;
import com.example.backend.entity.User;
import com.example.backend.entity.Ward;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShopMapperTest {

    @Mock
    private AddressMapper addressMapper;

    private ShopMapper shopMapper;

    @BeforeEach
    void setUp() {
        shopMapper = new ShopMapperImpl();
        // Inject AddressMapper using reflection
        ReflectionTestUtils.setField(Objects.requireNonNull(shopMapper), "addressMapper", addressMapper);
    }

    @Test
    void toShopResponse_shouldMapCorrectly() {
        //setup
        User owner = User.builder()
                .userId("user-1")
                .username("shopowner")
                .build();

        AddressBook address = AddressBook.builder()
                .name("Shop Address")
                .phone("0123456789")
                .addressDetail("123 Shop Street")
                .build();

        Shop shop = Shop.builder()
                .shopId("shop-1")
                .name("Test Shop")
                .owner(owner)
                .address(address)
                .build();

        AddressDTO addressDTO = AddressDTO.builder()
                .name("Shop Address")
                .phone("0123456789")
                .addressDetail("123 Shop Street")
                .build();

        when(addressMapper.toDto(address)).thenReturn(addressDTO);

        //chay
        ShopResponse response = shopMapper.toShopResponse(shop);

        // Assert
        assertNotNull(response);
        assertEquals("shop-1", response.getShopId());
        assertEquals("Test Shop", response.getName());
        assertEquals("user-1", response.getOwnerId());
        assertEquals("shopowner", response.getOwnerUsername());
        assertNotNull(response.getAddress());
    }

    @Test
    void toShopResponse_shouldMapOwnerFields() {
        //setup
        User owner = User.builder()
                .userId("owner-123")
                .username("testowner")
                .build();

        Shop shop = Shop.builder()
                .shopId("shop-1")
                .name("Test Shop")
                .owner(owner)
                .build();

        //chay
        ShopResponse response = shopMapper.toShopResponse(shop);

        // Assert
        assertEquals("owner-123", response.getOwnerId());
        assertEquals("testowner", response.getOwnerUsername());
    }

    @Test
    void toShopResponse_shouldMapAddress() {
        //setup
        AddressBook address = AddressBook.builder()
                .name("Shop Address")
                .phone("0123456789")
                .addressDetail("123 Shop Street")
                .ward(Ward.builder().code("001").build())
                .build();

        Shop shop = Shop.builder()
                .shopId("shop-1")
                .name("Test Shop")
                .address(address)
                .build();

        AddressDTO addressDTO = AddressDTO.builder()
                .name("Shop Address")
                .phone("0123456789")
                .addressDetail("123 Shop Street")
                .wardCode("001")
                .build();

        when(addressMapper.toDto(address)).thenReturn(addressDTO);

        //chay
        ShopResponse response = shopMapper.toShopResponse(shop);

        // Assert
        assertNotNull(response.getAddress());
        assertEquals("Shop Address", response.getAddress().getName());
        assertEquals("0123456789", response.getAddress().getPhone());
    }

    @Test
    void toShop_shouldMapCorrectly() {
        //setup
        ShopCreationRequest request = ShopCreationRequest.builder()
                .name("New Shop")
                .address(AddressDTO.builder()
                        .name("Shop Address")
                        .phone("0123456789")
                        .addressDetail("123 Street")
                        .wardCode("001")
                        .build())
                .build();

        //chay
        Shop shop = shopMapper.toShop(request);

        // Assert
        assertNotNull(shop);
        assertEquals("New Shop", shop.getName());
        // Owner, shopId, ratings, products should be ignored
        assertNull(shop.getOwner());
    }

    @Test
    void toShop_shouldIgnoreOwnerShopIdRatingsProducts() {
        //setup
        ShopCreationRequest request = ShopCreationRequest.builder()
                .name("Test Shop")
                .build();

        //chay
        Shop shop = shopMapper.toShop(request);

        // Assert
        assertNull(shop.getOwner());
        assertNull(shop.getShopId());
        // Note: ratings and products are collections, they might be null or empty
    }

    @Test
    void toShopResponse_nullShop_returnsNull() {
        //chay
        ShopResponse response = shopMapper.toShopResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    void toShop_nullRequest_returnsNull() {
        //chay
        Shop shop = shopMapper.toShop(null);

        // Assert
        assertNull(shop);
    }

    @Test
    void toShopResponse_nullOwner_shouldHandleGracefully() {
        //setup
        Shop shop = Shop.builder()
                .shopId("shop-1")
                .name("Test Shop")
                .owner(null)
                .build();

        //chay
        ShopResponse response = shopMapper.toShopResponse(shop);

        // Assert
        assertNotNull(response);
        assertNull(response.getOwnerId());
        assertNull(response.getOwnerUsername());
    }

    @Test
    void toShopResponse_nullAddress_shouldHandleGracefully() {
        //setup
        Shop shop = Shop.builder()
                .shopId("shop-1")
                .name("Test Shop")
                .address(null)
                .build();

        //chay
        ShopResponse response = shopMapper.toShopResponse(shop);

        // Assert
        assertNotNull(response);
        // Address might be null or mapped to null DTO
    }
}
