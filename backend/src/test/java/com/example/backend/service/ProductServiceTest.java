package com.example.backend.service;

import com.example.backend.dto.request.ProductCreationRequest;
import com.example.backend.dto.response.ProductResponse;
import com.example.backend.entity.Category;
import com.example.backend.entity.Product;
import com.example.backend.entity.Shop;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.mapper.ProductMapper;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ProductImageRepository;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.ShopRepository;
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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ProductRepository productRepository;

    @Mock
    ShopRepository shopRepository;

    @Mock
    ProductMapper productMapper;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    ProductImageRepository productImageRepository;

    @Mock
    ProductImageService productImageService;

    @InjectMocks
    ProductService productService;

    private Shop testShop;
    private User shopOwner;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        shopOwner = User.builder()
                .userId("owner-1")
                .username("owner")
                .email("owner@sis.hust.edu.vn")
                .build();

        testShop = Shop.builder()
                .shopId("shop-1")
                .name("Test Shop")
                .owner(shopOwner)
                .build();

        testProduct = Product.builder()
                .productId("product-1")
                .name("Test Product")
                .price(100000)
                .weight(500)
                .brand("Test Brand")
                .description("Test Description")
                .shop(testShop)
                .build();
    }

    @Test
    @SuppressWarnings("null")
    void createProduct_success_asOwner() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("owner");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            ProductCreationRequest request = new ProductCreationRequest();
            request.setShopId("shop-1");
            request.setName("New Product");
            request.setPrice(150000);
            request.setWeight(600);
            request.setBrand("New Brand");
            request.setDescription("New Description");
            request.setCategoryNames(Set.of("Electronics"));

            Product newProduct = Product.builder()
                    .name("New Product")
                    .price(150000)
                    .build();

            ProductResponse response = ProductResponse.builder()
                    .productId("product-2")
                    .name("New Product")
                    .price(150000)
                    .build();

            when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
            when(categoryRepository.findByName("electronics")).thenReturn(Optional.empty());
            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
                Category cat = invocation.getArgument(0);
                return cat;
            });
            when(productMapper.toProduct(request)).thenReturn(newProduct);
            when(productRepository.save(any(Product.class))).thenAnswer(invocation ->
                    (Product) Objects.requireNonNull(invocation.getArgument(0))
            );
            when(productMapper.toProductResponse(any(Product.class))).thenReturn(response);

            ProductResponse result = productService.createProduct(request);

            assertNotNull(result);
            assertEquals("New Product", result.getName());
            verify(productRepository).save(any(Product.class));
        }
    }

    @Test
    @SuppressWarnings("null")
    void createProduct_success_asAdmin() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("admin");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(true);

            ProductCreationRequest request = new ProductCreationRequest();
            request.setShopId("shop-1");
            request.setName("New Product");
            request.setCategoryNames(Set.of("Electronics"));

            Product newProduct = Product.builder()
                    .name("New Product")
                    .build();

            ProductResponse response = ProductResponse.builder()
                    .productId("product-2")
                    .name("New Product")
                    .build();

            when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
            when(categoryRepository.findByName("electronics")).thenReturn(Optional.empty());
            when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
                Category cat = Objects.requireNonNull(invocation.getArgument(0));
                return cat;
            });
            when(productMapper.toProduct(request)).thenReturn(newProduct);
            when(productRepository.save(any(Product.class))).thenReturn(newProduct);
            when(productMapper.toProductResponse(newProduct)).thenReturn(response);

            ProductResponse result = productService.createProduct(request);

            assertNotNull(result);
            verify(productRepository).save(any(Product.class));
        }
    }

    @Test
    void createProduct_shopNotExist() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("owner");

            ProductCreationRequest request = new ProductCreationRequest();
            request.setShopId("non-existent");

            when(shopRepository.findById("non-existent")).thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class, () -> {
                productService.createProduct(request);
            });

            assertEquals(ErrorCode.SHOP_NOT_EXIST, exception.getErrorCode());
        }
    }

    @Test
    void createProduct_unauthorized() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("other-user");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            ProductCreationRequest request = new ProductCreationRequest();
            request.setShopId("shop-1");

            when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));

            AppException exception = assertThrows(AppException.class, () -> {
                productService.createProduct(request);
            });

            assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        }
    }

    @Test
    void getProductById_success() {
        ProductResponse response = ProductResponse.builder()
                .productId("product-1")
                .name("Test Product")
                .build();

        when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
        when(productMapper.toProductResponse(testProduct)).thenReturn(response);

        ProductResponse result = productService.getProductById("product-1");

        assertNotNull(result);
        assertEquals("product-1", result.getProductId());
    }

    @Test
    void getProductById_notFound() {
        when(productRepository.findById("non-existent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            productService.getProductById("non-existent");
        });

        assertEquals(ErrorCode.PRODUCT_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    void getAllProducts_success() {
        Product product2 = Product.builder()
                .productId("product-2")
                .name("Product 2")
                .build();

        ProductResponse response1 = ProductResponse.builder()
                .productId("product-1")
                .name("Test Product")
                .build();
        ProductResponse response2 = ProductResponse.builder()
                .productId("product-2")
                .name("Product 2")
                .build();

        when(shopRepository.findById("shop-1")).thenReturn(Optional.of(testShop));
        when(productRepository.findAllByShop(testShop)).thenReturn(List.of(testProduct, product2));
        when(productMapper.toProductResponse(testProduct)).thenReturn(response1);
        when(productMapper.toProductResponse(product2)).thenReturn(response2);

        List<ProductResponse> results = productService.getAllProducts("shop-1");

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void deleteProduct_success_asOwner() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("owner");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));
            doNothing().when(productRepository).delete(Objects.requireNonNull(testProduct));

            productService.deleteProduct("product-1");

            verify(productRepository).delete(Objects.requireNonNull(testProduct));
        }
    }

    @Test
    void deleteProduct_unauthorized() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("other-user");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            when(productRepository.findById("product-1")).thenReturn(Optional.of(testProduct));

            AppException exception = assertThrows(AppException.class, () -> {
                productService.deleteProduct("product-1");
            });

            assertEquals(ErrorCode.UNAUTHORIZED, exception.getErrorCode());
        }
    }

    @Test
    void getProductsByCategory_success() {
        ProductResponse response = ProductResponse.builder()
                .productId("product-1")
                .name("Test Product")
                .build();

        when(productRepository.findAllByCategories_Name("electronics"))
                .thenReturn(List.of(testProduct));
        when(productMapper.toProductResponse(testProduct)).thenReturn(response);

        List<ProductResponse> results = productService.getProductsByCategory("Electronics");

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void getProductsByBrand_success() {
        ProductResponse response = ProductResponse.builder()
                .productId("product-1")
                .brand("Samsung")
                .build();

        when(productRepository.findAllByBrandIgnoreCase("Samsung"))
                .thenReturn(List.of(testProduct));
        when(productMapper.toProductResponse(testProduct)).thenReturn(response);

        List<ProductResponse> results = productService.getProductsByBrand("Samsung");

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void searchProduct_success() {
        ProductResponse response = ProductResponse.builder()
                .productId("prod-1")
                .productName("Laptop Dell")
                .build();

        when(productRepository.findByProductNameContainingIgnoreCase("Laptop"))
                .thenReturn(List.of(testProduct));
        when(productMapper.toProductResponse(testProduct)).thenReturn(response);

        List<ProductResponse> results = productService.searchProduct("Laptop");

        assertNotNull(results);
        assertEquals(1, results.size());
    }

    // ================= BAN PRODUCT =================

    @Test
    void banProduct_success() {
        ProductResponse response = ProductResponse.builder()
                .productId("prod-1")
                .productName("Test Product")
                .status("BANNED")
                .build();

        when(productRepository.findById("prod-1")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any())).thenReturn(testProduct);
        when(productMapper.toProductResponse(any())).thenReturn(response);

        ProductResponse result = productService.banProduct("prod-1");

        assertNotNull(result);
        assertEquals("BANNED", result.getStatus());
        verify(productRepository).save(any());
    }

    @Test
    void banProduct_productNotExist() {
        when(productRepository.findById("non-existent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            productService.banProduct("non-existent");
        });

        assertEquals(ErrorCode.PRODUCT_NOT_EXIST, exception.getErrorCode());
    }

    // ================= UNBAN PRODUCT =================

    @Test
    void unbanProduct_success() {
        ProductResponse response = ProductResponse.builder()
                .productId("prod-1")
                .productName("Test Product")
                .status("ACTIVE")
                .build();

        when(productRepository.findById("prod-1")).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any())).thenReturn(testProduct);
        when(productMapper.toProductResponse(any())).thenReturn(response);

        ProductResponse result = productService.unbanProduct("prod-1");

        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
        verify(productRepository).save(any());
    }

    @Test
    void unbanProduct_productNotExist() {
        when(productRepository.findById("non-existent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            productService.unbanProduct("non-existent");
        });

        assertEquals(ErrorCode.PRODUCT_NOT_EXIST, exception.getErrorCode());
    }
}
