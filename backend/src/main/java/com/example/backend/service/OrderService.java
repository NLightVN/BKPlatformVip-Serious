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
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    public List<OrderResponse> checkoutSelectedItems(String userId, OrderSelectedItemsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        AddressBook userAddress = addressBookRepository.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Địa chỉ giao hàng không tồn tại"));

        Cart cart = user.getCart();
        if (cart == null || cart.getItems().isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        List<CartItem> selectedItems = cart.getItems().stream()
                .filter(item -> request.getCartItemIds().contains(item.getId()))
                .toList();

        if (selectedItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_EXIST);
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
            double shippingFee = 30000; // Mặc định
            if (shopAddress != null && shopAddress.getWard() != null && userAddress.getWard() != null) {
                try {
                    ShippingFeeRequest feeRequest = new ShippingFeeRequest();
                    feeRequest.setFromDistrictCode(shopAddress.getWard().getDistrict().getCode());
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

    public List<OrderResponse> getOrdersByUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        List<Order> orders = orderRepository.findAllByUser(user);
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
}