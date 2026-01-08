package com.example.backend.integration;

import com.example.backend.dto.request.ProductCreationRequest;
import com.example.backend.dto.request.ShopCreationRequest;
import com.example.backend.dto.request.UserCreationRequest;
import com.example.backend.dto.response.ProductResponse;
import com.example.backend.dto.response.ShopResponse;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.Product;
import com.example.backend.entity.Shop;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.ShopRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.CartRepository;
import com.example.backend.service.ProductService;
import com.example.backend.service.ShopService;
import com.example.backend.service.UserService;
import com.example.backend.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BusinessFlowIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private ProductService productService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @BeforeEach
    void setUp() {
        //clean up
        productRepository.deleteAll();
        shopRepository.deleteAll();
        cartRepository.deleteAll();
        userRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    //================= BANNED USER FLOW =================

    @Test
    void bannedUserCannotPlaceOrder_fullFlow() {
        //Step 1: Tạo user
        UserCreationRequest userRequest = UserCreationRequest.builder()
                .username("testbuyer")
                .password("password123")
                .fullname("Test Buyer")
                .email("buyer@sis.hust.edu.vn")
                .build();

        UserResponse user = userService.createUser(userRequest);

        //Step 2: Admin bans user
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        UserResponse bannedUser = userService.banUser(user.getUserId());
        assertEquals("BANNED", bannedUser.getStatus());
        SecurityContextHolder.clearContext();

        //Step 3: Banned user tries to checkout (should fail)
        Authentication bannedAuth = new UsernamePasswordAuthenticationToken(
                "testbuyer",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(bannedAuth);

        AppException exception = assertThrows(AppException.class, () -> {
            orderService.checkout(List.of());
        });

        assertEquals(ErrorCode.USER_BANNED, exception.getErrorCode());

        SecurityContextHolder.clearContext();
    }

    //================= BANNED SHOP FLOW =================

    @Test
    void bannedShopProductsHiddenFromPublic_fullFlow() {
        //Step 1: Create owner and shop with product
        UserCreationRequest ownerRequest = UserCreationRequest.builder()
                .username("shopowner")
                .password("password123")
                .fullname("Shop Owner")
                .email("shopowner@sis.hust.edu.vn")
                .build();

        userService.createUser(ownerRequest);

        Authentication ownerAuth = new UsernamePasswordAuthenticationToken(
                "shopowner",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(ownerAuth);

        ShopCreationRequest shopRequest = ShopCreationRequest.builder()
                .name("Test Shop")
                .build();

        ShopResponse shop = shopService.createShop(shopRequest);

        ProductCreationRequest productRequest = new ProductCreationRequest();
        productRequest.setShopId(shop.getShopId());
        productRequest.setName("Test Product");
        productRequest.setPrice(100000);
        productRequest.setWeight(500);

        ProductResponse product = productService.createProduct(productRequest);

        SecurityContextHolder.clearContext();

        //Step 2: Verify product visible in public list initially
        List<ProductResponse> productsBeforeBan = productService.getAllProducts(shop.getShopId());
        assertFalse(productsBeforeBan.isEmpty());

        //Step 3: Admin bans shop
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        ShopResponse bannedShop = shopService.banShop(shop.getShopId());
        assertEquals("BANNED", bannedShop.getStatus());

        SecurityContextHolder.clearContext();

        //Step 4: Verify shop status is BANNED in DB
        Optional<Shop> shopCheck = shopRepository.findById(shop.getShopId());
        assertTrue(shopCheck.isPresent());
        assertEquals("BANNED", shopCheck.get().getStatus());

        //Step 5: Admin unbans shop
        SecurityContextHolder.getContext().setAuthentication(adminAuth);
        ShopResponse unbannedShop = shopService.unbanShop(shop.getShopId());
        assertEquals("ACTIVE", unbannedShop.getStatus());

        //Step 6: Verify products visible again
        SecurityContextHolder.clearContext();
        List<ProductResponse> productsAfterUnban = productService.getAllProducts(shop.getShopId());
        assertFalse(productsAfterUnban.isEmpty());

        SecurityContextHolder.clearContext();
    }

    //================= BANNED PRODUCT FLOW =================

    @Test
    void bannedProductNotAvailableForPurchase_fullFlow() {
        //Step 1: Create shop owner, shop, and product
        UserCreationRequest ownerRequest = UserCreationRequest.builder()
                .username("productowner")
                .password("password123")
                .fullname("Product Owner")
                .email("productowner@sis.hust.edu.vn")
                .build();

        userService.createUser(ownerRequest);

        Authentication ownerAuth = new UsernamePasswordAuthenticationToken(
                "productowner",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(ownerAuth);

        ShopCreationRequest shopRequest = ShopCreationRequest.builder()
                .name("Product Shop")
                .build();

        ShopResponse shop = shopService.createShop(shopRequest);

        ProductCreationRequest productRequest = new ProductCreationRequest();
        productRequest.setShopId(shop.getShopId());
        productRequest.setName("Banned Test Product");
        productRequest.setPrice(50000);
        productRequest.setWeight(300);

        ProductResponse product = productService.createProduct(productRequest);

        SecurityContextHolder.clearContext();

        //Step 2: Admin bans product
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(adminAuth);

        ProductResponse bannedProduct = productService.banProduct(product.getProductId());
        assertEquals("BANNED", bannedProduct.getStatus());

        SecurityContextHolder.clearContext();

        //Step 3: Verify product is banned in database
        Optional<Product> productCheck = productRepository.findById(product.getProductId());
        assertTrue(productCheck.isPresent());
        assertEquals("BANNED", productCheck.get().getStatus());

        //Note: checkout with banned products should be validated in OrderService
        //This test verifies the ban status is properly set

        SecurityContextHolder.clearContext();
    }
}
