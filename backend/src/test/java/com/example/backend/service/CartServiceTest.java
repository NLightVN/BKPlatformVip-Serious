package com.example.backend.service;

import com.example.backend.dto.request.CartRequest;
import com.example.backend.dto.response.CartResponse;
import com.example.backend.entity.Cart;
import com.example.backend.entity.CartItem;
import com.example.backend.entity.Product;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.mapper.CartMapper;
import com.example.backend.repository.CartRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    CartRepository cartRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    CartMapper cartMapper;

    @InjectMocks
    CartService cartService;

    private User testUser;
    private Product testProduct;
    private Cart testCart;
    private CartItem testCartItem;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId("user-1")
                .username("testuser")
                .email("test@sis.hust.edu.vn")
                .build();

        testProduct = Product.builder()
                .productId("product-1")
                .name("Test Product")
                .price(100000)
                .weight(500)
                .build();

        testCart = Cart.builder()
                .id("cart-1")
                .user(testUser)
                .totalAmount(0)
                .items(new ArrayList<>())
                .build();

        testCartItem = CartItem.builder()
                .id("item-1")
                .cart(testCart)
                .product(testProduct)
                .quantity(2)
                .build();
    }

    @Test
    @SuppressWarnings("null")
    void addToCart_success_newCart() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            CartRequest request = CartRequest.builder()
                    .productId("product-1")
                    .quantity(2)
                    .build();

            CartResponse response = CartResponse.builder()
                    .id("cart-1")
                    .totalAmount(200000)
                    .build();

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
                Cart cart = invocation.getArgument(0, Cart.class);
                cart.setId("cart-1");
                return cart;
            });
            when(cartRepository.saveAndFlush(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0, Cart.class));
            when(cartMapper.toCartResponse(any(Cart.class))).thenReturn(response);

            CartResponse result = cartService.addToCart(request, "user-1");

            assertNotNull(result);
            verify(cartRepository).save(any(Cart.class));
            verify(cartRepository).saveAndFlush(any(Cart.class));
        }
    }

    @Test
    @SuppressWarnings("null")
    void addToCart_success_existingCart() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            testUser.setCart(testCart);
            testCart.getItems().add(testCartItem);

            CartRequest request = CartRequest.builder()
                    .productId("product-1")
                    .quantity(1)
                    .build();

            CartResponse response = CartResponse.builder()
                    .id("cart-1")
                    .totalAmount(300000)
                    .build();

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
            when(cartRepository.saveAndFlush(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0, Cart.class));
            when(cartMapper.toCartResponse(any(Cart.class))).thenReturn(response);

            CartResponse result = cartService.addToCart(request, "user-1");

            assertNotNull(result);
            verify(cartRepository).saveAndFlush(any(Cart.class));
        }
    }

    @Test
    void addToCart_invalidQuantity() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            CartRequest request = CartRequest.builder()
                    .productId("product-1")
                    .quantity(0)
                    .build();

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));

            AppException exception = assertThrows(AppException.class, () -> {
                cartService.addToCart(request, "user-1");
            });

            assertEquals(ErrorCode.INVALID_VALUE, exception.getErrorCode());
        }
    }

    @Test
    void addToCart_productNotExist() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            CartRequest request = CartRequest.builder()
                    .productId("non-existent")
                    .quantity(1)
                    .build();

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(productRepository.findById("non-existent")).thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class, () -> {
                cartService.addToCart(request, "user-1");
            });

            assertEquals(ErrorCode.PRODUCT_NOT_EXIST, exception.getErrorCode());
        }
    }

    @Test
    @SuppressWarnings("null")
    void removeFromCart_success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            testUser.setCart(testCart);
            testCart.getItems().add(testCartItem);

            CartResponse response = CartResponse.builder()
                    .id("cart-1")
                    .totalAmount(0)
                    .build();

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0, Cart.class));
            when(cartMapper.toCartResponse(any(Cart.class))).thenReturn(response);

            CartResponse result = cartService.removeFromCart("user-1", "product-1");

            assertNotNull(result);
            verify(cartRepository).save(any(Cart.class));
        }
    }

    @Test
    void removeFromCart_cartEmpty() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            testUser.setCart(testCart);

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));

            AppException exception = assertThrows(AppException.class, () -> {
                cartService.removeFromCart("user-1", "product-1");
            });

            assertEquals(ErrorCode.CART_EMPTY, exception.getErrorCode());
        }
    }

    @Test
    void removeFromCart_itemNotExist() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            testUser.setCart(testCart);
            CartItem otherItem = CartItem.builder()
                    .id("item-2")
                    .cart(testCart)
                    .product(Product.builder().productId("product-2").build())
                    .quantity(1)
                    .build();
            testCart.getItems().add(otherItem);

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));

            AppException exception = assertThrows(AppException.class, () -> {
                cartService.removeFromCart("user-1", "product-1");
            });

            assertEquals(ErrorCode.CART_ITEM_NOT_EXIST, exception.getErrorCode());
        }
    }

    @Test
    @SuppressWarnings("null")
    void clearCart_success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            testUser.setCart(testCart);
            testCart.getItems().add(testCartItem);

            CartResponse response = CartResponse.builder()
                    .id("cart-1")
                    .totalAmount(0)
                    .build();

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0, Cart.class));
            when(cartMapper.toCartResponse(any(Cart.class))).thenReturn(response);

            CartResponse result = cartService.clearCart("user-1");

            assertNotNull(result);
            verify(cartRepository).save(any(Cart.class));
        }
    }

    @Test
    void clearCart_cartEmpty() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));

            AppException exception = assertThrows(AppException.class, () -> {
                cartService.clearCart("user-1");
            });

            assertEquals(ErrorCode.CART_EMPTY, exception.getErrorCode());
        }
    }

    @Test
    void getCartByUser_success_existingCart() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            testUser.setCart(testCart);

            CartResponse response = CartResponse.builder()
                    .id("cart-1")
                    .totalAmount(200000)
                    .build();

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(cartMapper.toCartResponse(testCart)).thenReturn(response);

            CartResponse result = cartService.getCartByUser("user-1");

            assertNotNull(result);
            assertEquals("cart-1", result.getId());
        }
    }

    @Test
    @SuppressWarnings("null")
    void getCartByUser_success_createNewCart() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            CartResponse response = CartResponse.builder()
                    .id("cart-1")
                    .totalAmount(0)
                    .build();

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
                Cart cart = invocation.getArgument(0, Cart.class);
                cart.setId("cart-1");
                return cart;
            });
            when(cartMapper.toCartResponse(any(Cart.class))).thenReturn(response);

            CartResponse result = cartService.getCartByUser("user-1");

            assertNotNull(result);
            verify(cartRepository).save(any(Cart.class));
        }
    }

    @Test
    void getCartByUser_unauthorized() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("other-user");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));

            AppException exception = assertThrows(AppException.class, () -> {
                cartService.getCartByUser("user-1");
            });

            assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        }
    }
}
