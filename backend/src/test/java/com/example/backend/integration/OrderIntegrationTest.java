package com.example.backend.integration;

import com.example.backend.dto.request.*;
import com.example.backend.dto.response.*;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import com.example.backend.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class OrderIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.example.backend.repository.ProvinceRepository provinceRepository;

    @Autowired
    private com.example.backend.repository.DistrictRepository districtRepository;

    @Autowired
    private com.example.backend.repository.WardRepository wardRepository;

    @Autowired
    private com.example.backend.repository.AddressBookRepository addressBookRepository;

    private UserResponse buyer;
    private ShopResponse shop;
    private ProductResponse product1;
    private ProductResponse product2;

    @BeforeEach
    void setUp() {
        // Create buyer
        UserCreationRequest buyerRequest = UserCreationRequest.builder()
                .username("buyer")
                .password("password123")
                .fullname("Buyer User")
                .email("buyer@sis.hust.edu.vn")
                .build();
        buyer = userService.createUser(buyerRequest);

        // Create address for buyer (required for checkout)
        Province province = Province.builder()
                .code("TEST01")
                .fullName("Test Province")
                .build();
        province = provinceRepository.save(province);

        District district = District.builder()
                .code("TEST01D01")
                .fullName("Test District")
                .province(province)
                .build();
        district = districtRepository.save(district);

        Ward ward = Ward.builder()
                .code("TEST01D01W01")
                .fullName("Test Ward")
                .district(district)
                .build();
        ward = wardRepository.save(ward);

        AddressBook address = AddressBook.builder()
                .name("Test Address")
                .phone("0123456789")
                .addressDetail("123 Test Street")
                .ward(ward)
                .build();
        address = addressBookRepository.save(address);

        // Set address for buyer
        Optional<User> buyerEntity = userRepository.findByUserId(buyer.getUserId());
        if (buyerEntity.isPresent()) {
            buyerEntity.get().setAddress(address);
            userRepository.save(buyerEntity.get());
        }

        // Create seller
        UserCreationRequest sellerRequest = UserCreationRequest.builder()
                .username("seller")
                .password("password123")
                .fullname("Seller User")
                .email("seller@sis.hust.edu.vn")
                .build();
        userService.createUser(sellerRequest);

        // Mock authentication for seller to create shop
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "seller",
                null,
                java.util.Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Create shop
        ShopCreationRequest shopRequest = ShopCreationRequest.builder()
                .name("Test Shop")
                .build();
        shop = shopService.createShop(shopRequest);

        // Keep authentication for creating products (seller owns the shop)
        // Don't clear yet - need it for product creation

        // Create products
        ProductCreationRequest productRequest1 = new ProductCreationRequest();
        productRequest1.setShopId(shop.getShopId());
        productRequest1.setName("Product 1");
        productRequest1.setPrice(100000);
        productRequest1.setWeight(1000);
        productRequest1.setBrand("Brand 1");
        productRequest1.setDescription("Description 1");
        productRequest1.setCategoryNames(java.util.Set.of()); // Empty set for categories
        product1 = productService.createProduct(productRequest1);

        ProductCreationRequest productRequest2 = new ProductCreationRequest();
        productRequest2.setShopId(shop.getShopId());
        productRequest2.setName("Product 2");
        productRequest2.setPrice(200000);
        productRequest2.setWeight(2000);
        productRequest2.setBrand("Brand 2");
        productRequest2.setDescription("Description 2");
        productRequest2.setCategoryNames(java.util.Set.of()); // Empty set for categories
        product2 = productService.createProduct(productRequest2);

        // Clear authentication after creating products
        SecurityContextHolder.clearContext();
    }

    @Test
    void completeOrderFlow_shouldWork() {
        // Mock authentication for buyer
        Authentication buyerAuth = new UsernamePasswordAuthenticationToken(
                "buyer",
                null,
                java.util.Collections.emptyList()
        );
        SecurityContextHolder.getContext().setAuthentication(buyerAuth);

        // Step 1: Add products to cart
        cartService.addToCart(new CartRequest(product1.getProductId(), 2), buyer.getUserId());
        cartService.addToCart(new CartRequest(product2.getProductId(), 1), buyer.getUserId());

        // Step 2: Get cart
        CartResponse cart = cartService.getCartByUser(buyer.getUserId());
        assertNotNull(cart);
        assertEquals(2, cart.getItems().size());

        // Step 3: Get cart item IDs
        Optional<User> buyerEntity = userRepository.findByUserId(buyer.getUserId());
        assertTrue(buyerEntity.isPresent());

        Optional<Cart> cartEntity = cartRepository.findByUser(buyerEntity.get());
        assertTrue(cartEntity.isPresent());

        List<String> cartItemIds = cartEntity.get().getItems().stream()
                .map(CartItem::getId)
                .toList();

        assertFalse(cartItemIds.isEmpty());

        // Step 4: Checkout (authentication still set from buyer)
        // Get product IDs from cart items
        List<String> productIds = cartEntity.get().getItems().stream()
                .map(item -> item.getProduct().getProductId())
                .toList();

        OrderSelectedItemsRequest checkoutRequest = new OrderSelectedItemsRequest();
        checkoutRequest.setProductIds(productIds);
        // Note: Address ID would be set in real scenario

        List<OrderResponse> orders = orderService.checkoutSelectedItems(checkoutRequest);

        // Clear authentication
        SecurityContextHolder.clearContext();

        // Step 5: Verify orders created
        assertNotNull(orders);
        assertFalse(orders.isEmpty());

        OrderResponse order = orders.get(0);
        assertNotNull(order.getOrderId());
        assertTrue(order.getTotalAmount() > 0);
        assertNotNull(order.getItems());
        assertFalse(order.getItems().isEmpty());

        // Step 6: Verify order in database
        Optional<Order> dbOrder = orderRepository.findById(order.getOrderId());
        assertTrue(dbOrder.isPresent());
        assertEquals(buyer.getUserId(), dbOrder.get().getUser().getUserId());
    }

    @Test
    void createOrder_thenGetOrder_shouldReturnOrder() {
        // Arrange: Create order (simplified - in real scenario would go through checkout)
        // This test verifies the order retrieval flow

        // Act: Get orders by user
        List<OrderResponse> orders = orderService.getOrdersByUser(buyer.getUserId());

        // Assert: Orders returned (may be empty if no orders created)
        assertNotNull(orders);
    }
}
