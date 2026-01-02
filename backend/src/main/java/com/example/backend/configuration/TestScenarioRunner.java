//package com.example.backend.configuration;
//
//import com.example.backend.dto.request.*;
//import com.example.backend.dto.response.*;
//import com.example.backend.entity.CartItem;
//import com.example.backend.entity.Ward;
//import com.example.backend.repository.*;
//import com.example.backend.service.*;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.annotation.Order;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.PlatformTransactionManager;
//import org.springframework.transaction.support.TransactionTemplate;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Component
//@RequiredArgsConstructor
//@Slf4j
//@Order(2)
//public class TestScenarioRunner implements CommandLineRunner {
//
////    private final UserService userService;
////    private final ShopService shopService;
////    private final ProductService productService;
////    private final CartService cartService;
////    private final OrderService orderService;
////    private final ShippingService shippingService; // Thêm để test API lẻ
////
////    private final WardRepository wardRepository;
////    private final UserRepository userRepository;
////    private final CartRepository cartRepository;
////    private final PlatformTransactionManager transactionManager;
////
////    private final Scanner scanner = new Scanner(System.in);
////
////    // ANSI Colors
////    private final String GREEN = "\u001B[32m";
////    private final String RED = "\u001B[31m";
////    private final String YELLOW = "\u001B[33m";
////    private final String CYAN = "\u001B[36m";
////    private final String RESET = "\u001B[0m";
////
////    @Override
////    public void run(String... args) throws Exception {
////        System.out.println(YELLOW + "\n==============================================");
////        System.out.println("   NEONDB E-COMMERCE CLI TESTING INTERFACE");
////        System.out.println("==============================================" + RESET);
////
////        new Thread(() -> {
////            try {
////                Thread.sleep(2000); // Đợi server khởi động xong
////                while (true) {
////                    System.out.println("\n" + CYAN + "--- MENU TEST ---" + RESET);
////                    System.out.println("1. Chạy FULL SCENARIO (User -> Shop -> Product -> Cart -> Order + Ship)");
////                    System.out.println("2. Test Riêng API Tính Phí Ship (GHN)");
////                    System.out.println("0. Thoát CLI");
////                    System.out.print("Nhập lựa chọn: ");
////
////                    if (scanner.hasNextLine()) {
////                        String choice = scanner.nextLine();
////                        switch (choice) {
////                            case "1" -> runFullAutoTest();
////                            case "2" -> testShippingApiManual();
////                            case "0" -> {
////                                System.out.println("Đã thoát CLI.");
////                                return;
////                            }
////                            default -> System.out.println(RED + "Lựa chọn không hợp lệ" + RESET);
////                        }
////                    }
////                }
////            } catch (Exception e) {
////                log.error(e.getMessage());
////            }
////        }).start();
////    }
////
////    private void runFullAutoTest() {
////        System.out.println(GREEN + "\n>>> BẮT ĐẦU KỊCH BẢN TEST TOÀN BỘ..." + RESET);
////
////        //Chuẩn bị địa chỉ
////        List<Ward> wards = getSampleWards(2);
////        if (wards.size() < 2) {
////            System.out.println(RED + "LỖI: Database thiếu dữ liệu Ward. Hãy chạy LocationSeeder trước!" + RESET);
////            return;
////        }
////
////        AddressDTO shopAddress = createAddressDTO(wards.get(0), "Kho hàng Shop Vip");
////        AddressDTO buyerAddress = createAddressDTO(wards.get(1), "Nhà riêng Khách Hàng");
////
////        String timeId = String.valueOf(System.currentTimeMillis());
////        String uniqueSuffix = timeId.substring(timeId.length() - 5);
////
////        //Tạo User & Shop
////        System.out.println(CYAN + "\n[STEP 1] Tạo User & Shop với địa chỉ đầy đủ..." + RESET);
////        UserResponse seller = createUser("seller." + uniqueSuffix, "12345678", "Chủ Shop Test", shopAddress);
////        UserResponse buyer = createUser("buyer." + uniqueSuffix, "12345678", "Khách Mua Test", buyerAddress);
////
////        mockLogin(seller.getUsername());
////        ShopResponse shop = createShop("Shop Công Nghệ " + uniqueSuffix, shopAddress);
////
////        // Tạo Product (Có cân nặng để tính ship)
////        System.out.println(CYAN + "\n[STEP 2] Upload Sản Phẩm (kèm cân nặng)..." + RESET);
////        ProductResponse p1 = createProduct(shop.getShopId(), "Laptop Gaming Dell", 25000000, 2500); // 2.5kg
////        ProductResponse p2 = createProduct(shop.getShopId(), "Chuột Không Dây", 500000, 200);      // 200g
////
////        //  Thao tác Giỏ Hàng
////        System.out.println(CYAN + "\n[STEP 3] Khách hàng thêm vào giỏ..." + RESET);
////        mockLogin(buyer.getUsername());
////        cartService.addToCart(new CartRequest(p1.getProductId(), 1), buyer.getUserId());
////        cartService.addToCart(new CartRequest(p2.getProductId(), 2), buyer.getUserId());
////
////        CartResponse cart = cartService.getCartByUser(buyer.getUserId());
////        System.out.println("Giỏ hàng: " + cart.getItems().size() + " loại sản phẩm.");
////        System.out.println("Tổng tiền tạm tính (Cart không lưu snapshot): " + formatCurrency(cart.getTotalAmount()));
////
////        // Checkout (Tạo Order + Snapshot + Tính Ship)
////        System.out.println(CYAN + "\n[STEP 4] Checkout & Tính Ship tự động..." + RESET);
////
////        // Map ProductID -> CartItemID
////        List<String> cartItemIds = cart.getItems().stream()
////                .map(item -> findCartItemIdByProductId(buyer.getUserId(), item.getProductId()))
////                .filter(Objects::nonNull)
////                .collect(Collectors.toList());
////
////        if (cartItemIds.isEmpty()) {
////            System.out.println(RED + "LỖI: Giỏ hàng rỗng hoặc không tìm thấy Item ID!" + RESET);
////            return;
////        }
////
////        OrderSelectedItemsRequest checkoutRequest = new OrderSelectedItemsRequest();
////        checkoutRequest.setCartItemIds(cartItemIds);
////        checkoutRequest.setAddressId(getRealAddressId(buyer.getUsername())); // Lấy ID địa chỉ thật trong DB
////
////        try {
////            List<OrderResponse> orders = orderService.checkoutSelectedItems(buyer.getUserId(), checkoutRequest);
////
////            System.out.println(GREEN + "\n>>> ĐẶT HÀNG THÀNH CÔNG!" + RESET);
////            for (OrderResponse order : orders) {
////                System.out.println("------------------------------------------------");
////                System.out.println("Mã đơn hàng : " + order.getOrderId());
////                System.out.println("Tổng thanh toán : " + formatCurrency(order.getTotalAmount()));
////                System.out.println("Chi tiết Order Item (Có Snapshot giá):");
////                order.getItems().forEach(item ->
////                        System.out.println("  - " + item.getProductName() + " x" + item.getQuantity()
////                                + " | Snapshot giá lúc mua: " + formatCurrency(item.getPriceAtPurchase()))
////                );
////                System.out.println("------------------------------------------------");
////
////                // Validate Snapshot
////                if (order.getItems().get(0).getPriceAtPurchase() > 0) {
////                    System.out.println(GREEN + "✔ Snapshot giá đã được lưu vào Order Item thành công." + RESET);
////                } else {
////                    System.out.println(RED + "✘ Lỗi: Order Item chưa lưu giá snapshot!" + RESET);
////                }
////            }
////        } catch (Exception e) {
////            System.out.println(RED + "LỖI CHECKOUT: " + e.getMessage() + RESET);
////            e.printStackTrace();
////        }
////
////        System.out.println(GREEN + "\n✅ KỊCH BẢN TEST HOÀN TẤT!" + RESET);
////    }
////
////    private void testShippingApiManual() {
////        System.out.println(YELLOW + "\n>>> TEST RIÊNG API SHIP (GHN)..." + RESET);
////        List<Ward> wards = getSampleWards(2);
////        if (wards.size() < 2) return;
////
////        Ward from = wards.get(0);
////        Ward to = wards.get(1);
////
////        System.out.printf("Tuyến: %s (%s) -> %s (%s)%n",
////                from.getDistrict().getFullName(), from.getDistrict().getProvince().getFullName(),
////                to.getDistrict().getFullName(), to.getDistrict().getProvince().getFullName());
////
////        ShippingFeeRequest req = new ShippingFeeRequest();
////        req.setFromDistrictCode(from.getDistrict().getCode());
////        req.setToDistrictCode(to.getDistrict().getCode());
////        req.setToWardCode(to.getCode());
////        req.setWeightGram(1500); // 1.5kg
////
////        try {
////            ShippingFeeResponse res = shippingService.calculateShippingFee(req);
////            System.out.println(GREEN + "Kết quả: " + formatCurrency(res.getFee()) + " | Đơn vị: " + res.getProvider() + RESET);
////            System.out.println("Dự kiến giao: " + res.getEstimatedDays() + " ngày");
////        } catch (Exception e) {
////            System.out.println(RED + "API Lỗi: " + e.getMessage() + RESET);
////        }
////    }
////
////
////    private UserResponse createUser(String username, String password, String fullname, AddressDTO address) {
////        System.out.print("Đang tạo user: " + username + "...");
////        UserCreationRequest req = UserCreationRequest.builder()
////                .username(username)
////                .password(password)
////                .fullname(fullname)
////                .email(username + "@sis.hust.edu.vn")
////                .address(address)
////                .build();
////        UserResponse res = userService.createUser(req);
////        System.out.println(GREEN + " OK" + RESET);
////        return res;
////    }
////
////    private ShopResponse createShop(String name, AddressDTO address) {
////        System.out.print("Đang tạo shop: " + name + "...");
////        ShopCreationRequest req = ShopCreationRequest.builder()
////                .name(name)
////                .address(address)
////                .build();
////        ShopResponse res = shopService.createShop(req);
////        System.out.println(GREEN + " OK" + RESET);
////        return res;
////    }
////
////    private ProductResponse createProduct(String shopId, String name, double price, double weight) {
////        System.out.print(" + SP: " + name + " (" + weight + "g)...");
////        ProductCreationRequest req = new ProductCreationRequest();
////        req.setShopId(shopId);
////        req.setName(name);
////        req.setPrice(price);
////        req.setWeight(weight);
////        req.setBrand("TestBrand");
////        req.setDescription("Mô tả sản phẩm test");
////        req.setCategoryNames(Set.of("Dien Tu", "TestScenario"));
////
////        ProductResponse res = productService.createProduct(req);
////        System.out.println(GREEN + " OK" + RESET);
////        return res;
////    }
////
////    private AddressDTO createAddressDTO(Ward ward, String detail) {
////        return AddressDTO.builder()
////                .name("Tester")
////                .phone("0987654321")
////                .addressDetail(detail)
////                .wardCode(ward.getCode()) // Quan trọng để tính ship
////                .build();
////    }
////
////    private List<Ward> getSampleWards(int count) {
////        // Lấy 50 ward đầu tiên, rồi chọn cái đầu và cái cuối để hy vọng khác quận/tỉnh
////        Page<Ward> page = wardRepository.findAll(PageRequest.of(0, 50));
////        List<Ward> all = page.getContent();
////        if (all.size() < count) return all;
////
////        List<Ward> result = new ArrayList<>();
////        result.add(all.get(0));
////        if (count > 1) result.add(all.get(all.size() - 1));
////        return result;
////    }
////
////    // Fix Lazy Loading: Tìm CartItem trực tiếp từ DB
////    private String findCartItemIdByProductId(String userId, String productId) {
////        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
////        return transactionTemplate.execute(status -> {
////            var cart = cartRepository.findByUser(
////                    userRepository.findById(userId).orElse(null)
////            ).orElse(null);
////
////            if (cart != null && cart.getItems() != null) {
////                for (CartItem item : cart.getItems()) {
////                    if (item.getProduct().getProductId().equals(productId)) {
////                        return item.getId();
////                    }
////                }
////            }
////            return null;
////        });
////    }
////
////    private String getRealAddressId(String username) {
////        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
////        return transactionTemplate.execute(status ->
////                userRepository.findByUsername(username)
////                        .map(user -> user.getAddress().getAddressId())
////                        .orElse(null)
////        );
////    }
////
////    private void mockLogin(String username) {
////        SecurityContextHolder.getContext().setAuthentication(
////                new UsernamePasswordAuthenticationToken(username, null, new ArrayList<>())
////        );
////    }
////
////    private String formatCurrency(double amount) {
////        return String.format("%,.0f VND", amount);
////    }
//}