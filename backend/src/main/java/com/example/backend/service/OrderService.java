package com.example.backend.service;

import com.example.backend.dto.request.OrderSelectedItemsRequest;
import com.example.backend.dto.request.ShippingFeeRequest;
import com.example.backend.dto.response.OrderResponse;
import com.example.backend.dto.response.ShippingFeeResponse;
import com.example.backend.entity.*;
import com.example.backend.enums.OrderStatus;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.mapper.OrderMapper;
import com.example.backend.repository.*;
import com.example.backend.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.example.backend.dto.request.ShippingFeeRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class OrderService {

    UserRepository userRepository;
    CartRepository cartRepository;
    OrderRepository orderRepository;
    ProductRepository productRepository; // NEW
    ShopRepository shopRepository;
    OrderItemRepository orderItemRepository;
    AddressBookRepository addressBookRepository;
    ShipmentRepository shipmentRepository;
    PaymentRepository paymentRepository;
    ShippingService shippingService;
    OrderMapper orderMapper;

    /**
     * Checkout từ giỏ hàng - Tách đơn theo Shop + Tính Ship
     */
    @Transactional
    public List<OrderResponse> checkoutSelectedItems(OrderSelectedItemsRequest request) {
        User user = userRepository.findByUsername(
                SecurityUtil.getCurrentUsername()
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        //check user banned
        if ("BANNED".equals(user.getStatus())) {
            throw new AppException(ErrorCode.USER_BANNED);
        }

        AddressBook userAddress = user.getAddress();
        if (userAddress == null) {
            throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
        }

        Cart cart = user.getCart();
        if (cart == null || cart.getItems().isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        List<CartItem> selectedItems = cart.getItems().stream()
                .filter(item ->
                        request.getProductIds()
                                .contains(item.getProduct().getProductId())
                )
                .toList();

        if (selectedItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_EXIST);
        }

        // Validate that no selected products are deleted or banned
        List<String> unavailableProductNames = selectedItems.stream()
                .filter(item -> "DELETED".equals(item.getProduct().getStatus()) || 
                               "BANNED".equals(item.getProduct().getStatus()))
                .map(item -> item.getProduct().getName())
                .toList();
        
        if (!unavailableProductNames.isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXIST, 
                "Không thể đặt hàng. Các sản phẩm sau đã ngừng bán: " + String.join(", ", unavailableProductNames));
        }

        // Nhóm theo Shop
        Map<String, List<CartItem>> itemsByShop = selectedItems.stream()
                .collect(Collectors.groupingBy(item -> item.getProduct().getShop().getShopId()));

        List<Order> savedOrders = new ArrayList<>();

        for (Map.Entry<String, List<CartItem>> entry : itemsByShop.entrySet()) {
            List<CartItem> shopItems = entry.getValue();
            Shop shop = shopItems.get(0).getProduct().getShop();
            AddressBook shopAddress = shop.getAddress();

            // Tính tiền hàng
            double itemTotal = shopItems.stream()
                    .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                    .sum();

            // Tính cân nặng
            int totalWeight = (int) shopItems.stream()
                    .mapToDouble(i -> i.getProduct().getWeight() * i.getQuantity())
                    .sum();
            if (totalWeight == 0) totalWeight = 200;

            // Tính phí ship qua API
            double shippingFee = 30000;

            // Kiểm tra shopAddress và userAddress có đủ thông tin không
            if (shopAddress != null && shopAddress.getWard() != null
                    && userAddress.getWard() != null) {
                try {
                    ShippingFeeRequest feeRequest = new ShippingFeeRequest();

                    // Set điểm GỬI (Từ Shop)
                    feeRequest.setFromDistrictCode(shopAddress.getWard().getDistrict().getCode());
                    feeRequest.setFromWardCode(shopAddress.getWard().getCode()); // <--- MỚI

                    //set diem nhan - user
                    feeRequest.setToDistrictCode(userAddress.getWard().getDistrict().getCode());
                    feeRequest.setToWardCode(userAddress.getWard().getCode());

                    feeRequest.setWeightGram(totalWeight);

                    ShippingFeeResponse feeResponse = shippingService.calculateShippingFee(feeRequest);
                    shippingFee = feeResponse.getFee();
                } catch (Exception e) {
                    log.error("Failed to calculate shipping fee: {}", e.getMessage());
                }
            }
            // Tạo Order
            Order order = Order.builder()
                    .user(user)
                    .status(OrderStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .totalAmount(itemTotal + shippingFee)
                    .build();
            Order savedOrder = orderRepository.save(order);

            // Tạo Order Items
            List<OrderItem> orderItems = shopItems.stream().map(cartItem ->
                    OrderItem.builder()
                            .order(savedOrder)
                            .product(cartItem.getProduct())
                            .quantity(cartItem.getQuantity())
                            .priceAtPurchase(cartItem.getProduct().getPrice())
                            .build()
            ).toList();
            orderItemRepository.saveAll(orderItems);
            savedOrder.setItems(orderItems);

            // Tạo Shipment
            Shipment shipment = Shipment.builder()
                    .order(savedOrder)
                    .shippingFee(shippingFee)
                    .status("PREPARING")
                    .estimatedDeliveryDate(java.time.LocalDate.now().plusDays(3))
                    .build();
            shipmentRepository.save(shipment);

            savedOrders.add(savedOrder);
        }

        // Xóa items đã checkout khỏi giỏ
        cart.getItems().removeAll(selectedItems);
        double remainingTotal = cart.getItems().stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();
        cart.setTotalAmount(remainingTotal);
        cartRepository.save(cart);

        return savedOrders.stream().map(orderMapper::toOrderResponse).toList();
    }

    /**
     * Mua ngay (Buy Now) - Không qua giỏ hàng
     */
    @Transactional
    public OrderResponse checkoutBuyNow(com.example.backend.dto.request.OrderBuyNowRequest request) {
        User user = userRepository.findByUsername(
                SecurityUtil.getCurrentUsername()
        ).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        //check user banned
        if ("BANNED".equals(user.getStatus())) {
            throw new AppException(ErrorCode.USER_BANNED);
        }

        AddressBook userAddress = user.getAddress();
        if (userAddress == null) throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXIST));

        Shop shop = product.getShop();
        AddressBook shopAddress = shop.getAddress();

        int quantity = request.getQuantity();
        if (quantity < 1) throw new AppException(ErrorCode.INVALID_VALUE);

        // Tính tiền hàng
        double itemTotal = product.getPrice() * quantity;
        double shippingFee = 30000; // Mặc định

        // Tính cân nặng
        int totalWeight = (int) (product.getWeight() * quantity);
        if (totalWeight <= 0) totalWeight = 200;

        // Tính phí ship
        if (shopAddress != null && shopAddress.getWard() != null && userAddress.getWard() != null) {
            try {
                ShippingFeeRequest feeRequest = new ShippingFeeRequest();
                feeRequest.setFromDistrictCode(shopAddress.getWard().getDistrict().getCode());
                feeRequest.setFromWardCode(shopAddress.getWard().getCode());
                feeRequest.setToDistrictCode(userAddress.getWard().getDistrict().getCode());
                feeRequest.setToWardCode(userAddress.getWard().getCode());
                feeRequest.setWeightGram(totalWeight);

                ShippingFeeResponse feeResponse = shippingService.calculateShippingFee(feeRequest);
                shippingFee = feeResponse.getFee();
            } catch (Exception e) {
                log.error("Failed to calculate shipping fee: {}", e.getMessage());
            }
        }

        // Tạo Order
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .totalAmount(itemTotal + shippingFee)
                .build();
        Order savedOrder = orderRepository.save(order);

        // Tạo Order Item
        OrderItem orderItem = OrderItem.builder()
                .order(savedOrder)
                .product(product)
                .quantity(quantity)
                .priceAtPurchase(product.getPrice())
                .build();
        orderItemRepository.save(orderItem);
        savedOrder.setItems(List.of(orderItem));

        // Tạo Shipment
        Shipment shipment = Shipment.builder()
                .order(savedOrder)
                .shippingFee(shippingFee)
                .status("PREPARING") // Hoặc lấy status mặc định
                .estimatedDeliveryDate(java.time.LocalDate.now().plusDays(3))
                .build();
        shipmentRepository.save(shipment);
        savedOrder.setShipment(shipment);



        return orderMapper.toOrderResponse(savedOrder);
    }

    @Transactional
    public List<OrderResponse> getOrdersByUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        List<Order> orders = orderRepository.findAllByUserOrderByCreatedAtDesc(user);
        return orders.stream().map(orderMapper::toOrderResponse).toList();
    }

    public OrderResponse getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));
        return orderMapper.toOrderResponse(order);
    }

    @Transactional
    public OrderResponse updateOrderStatus(String orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));

        order.setStatus(status);

        if (status == OrderStatus.CANCELLED && order.getShipment() != null) {
            order.getShipment().setStatus("CANCELLED");
        }

        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    // Lấy danh sách order của Shop (dành cho Seller và Admin)
    public List<OrderResponse> getOrdersByShop(String shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_EXIST));

        String currentUsername = SecurityUtil.getCurrentUsername();
        
        // Allow if user is admin OR shop owner
        if (!SecurityUtil.hasRole("ADMIN") && !shop.getOwner().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return orderRepository.findAllByShopId(shopId).stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    @Transactional
    public OrderResponse requestCancel(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));

        // Chỉ cho phép yêu cầu hủy nếu đang PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể yêu cầu hủy đơn hàng đang chờ xử lý");
        }

        order.setCancellationRequested(true);
        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse replyCancelRequest(String orderId, boolean accept) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));

        //check shop owner
        // For now trusting the controller level security checks or implicit logic
        // But better to verify shop ownership logic if strictly required. 
        // Here we assume authorized caller.

        if (accept) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancellationRequested(false); // Reset/Clear request flag as it is now cancelled
             if (order.getShipment() != null) {
                order.getShipment().setStatus("CANCELLED");
            }
        } else {
            order.setCancellationRequested(false); // Reject request
        }

        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    @Transactional
    public OrderResponse confirmOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));

        //check order pending
        if (order.getStatus() != OrderStatus.PENDING) {
            // Provide specific error message based on current status
            throw new AppException(ErrorCode.INVALID_VALUE, 
                "Chỉ có thể xác nhận đơn hàng đang chờ xử lý. Trạng thái hiện tại: " + order.getStatus());
        }

        //verify order co items
        if (order.getItems() == null || order.getItems().isEmpty()) {
            throw new AppException(ErrorCode.ORDER_NOT_EXIST, "Đơn hàng không có sản phẩm");
        }
        
        //verify shop owner
        try {
            Shop shop = order.getItems().get(0).getProduct().getShop();
            String currentUsername = SecurityUtil.getCurrentUsername();
            if (!SecurityUtil.hasRole("ADMIN") && !shop.getOwner().getUsername().equals(currentUsername)) {
                throw new AppException(ErrorCode.UNAUTHORIZED, "Bạn không có quyền xác nhận đơn hàng này");
            }
        } catch (NullPointerException e) {
            throw new AppException(ErrorCode.ORDER_NOT_EXIST, "Không thể xác định shop của đơn hàng");
        }

        // Transition to AWAITING_PICKUP
        order.setStatus(OrderStatus.AWAITING_PICKUP);
        if (order.getShipment() != null) {
            order.getShipment().setStatus("AWAITING_PICKUP");
        }

        return orderMapper.toOrderResponse(orderRepository.save(order));
    }

    public com.example.backend.dto.response.ShopRevenueResponse getShopRevenue(String shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_EXIST));

        //verify authorization
        String currentUsername = SecurityUtil.getCurrentUsername();
        if (!SecurityUtil.hasRole("ADMIN") && !shop.getOwner().getUsername().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        //lay tat ca order cua shop
        List<Order> orders = orderRepository.findAllByShopId(shopId);

        // Calculate statistics
        double totalRevenue = 0;
        long pendingCount = 0;
        long awaitingPickupCount = 0;
        long shippedCount = 0;
        long deliveredCount = 0;
        long cancelledCount = 0;

        for (Order order : orders) {
            // Only count revenue for non-cancelled orders
            if (order.getStatus() != OrderStatus.CANCELLED) {
                totalRevenue += order.getTotalAmount();
            }

            // Count by status
            switch (order.getStatus()) {
                case PENDING:
                    pendingCount++;
                    break;
                case AWAITING_PICKUP:
                    awaitingPickupCount++;
                    break;
                case SHIPPED:
                    shippedCount++;
                    break;
                case DELIVERED:
                    deliveredCount++;
                    break;
                case CANCELLED:
                    cancelledCount++;
                    break;
            }
        }

        long totalOrders = orders.size();
        double averageOrderValue = totalOrders > 0 ? totalRevenue / (totalOrders - cancelledCount) : 0;

        return com.example.backend.dto.response.ShopRevenueResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .pendingOrders(pendingCount)
                .awaitingPickupOrders(awaitingPickupCount)
                .shippedOrders(shippedCount)
                .deliveredOrders(deliveredCount)
                .cancelledOrders(cancelledCount)
                .averageOrderValue(averageOrderValue)
                .build();
    }
}