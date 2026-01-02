package com.example.backend.service;

import com.example.backend.dto.response.OrderResponse;
import com.example.backend.entity.*;
import com.example.backend.enums.OrderStatus;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.mapper.OrderMapper;
import com.example.backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    CartRepository cartRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    OrderItemRepository orderItemRepository;

    @Mock
    AddressBookRepository addressBookRepository;

    @Mock
    ShipmentRepository shipmentRepository;

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    ShippingService shippingService;

    @Mock
    OrderMapper orderMapper;

    @InjectMocks
    OrderService orderService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private Shop testShop;
    private CartItem testCartItem;
    private AddressBook userAddress;
    private AddressBook shopAddress;

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
                .build();

        testProduct = Product.builder()
                .productId("product-1")
                .name("Test Product")
                .price(100000)
                .weight(500)
                .shop(testShop)
                .build();

        testCart = Cart.builder()
                .id("cart-1")
                .user(testUser)
                .totalAmount(200000)
                .items(new ArrayList<>())
                .build();

        testCartItem = CartItem.builder()
                .id("item-1")
                .cart(testCart)
                .product(testProduct)
                .quantity(2)
                .build();

        testCart.getItems().add(testCartItem);
        testUser.setCart(testCart);

        userAddress = AddressBook.builder()
                .addressId("addr-1")
                .name("User Address")
                .phone("0123456789")
                .addressDetail("123 User St")
                .build();

        shopAddress = AddressBook.builder()
                .addressId("addr-2")
                .name("Shop Address")
                .phone("0987654321")
                .addressDetail("456 Shop St")
                .build();

        testUser.setAddress(userAddress);
        testShop.setAddress(shopAddress);
    }

    @Test
    void getOrderById_success() {
        Order order = Order.builder()
                .id("order-1")
                .user(testUser)
                .status(OrderStatus.PENDING)
                .totalAmount(200000)
                .createdAt(LocalDateTime.now())
                .build();

        OrderResponse response = OrderResponse.builder()
                .orderId("order-1")
                .totalAmount(200000)
                .build();

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderMapper.toOrderResponse(order)).thenReturn(response);

        OrderResponse result = orderService.getOrderById("order-1");

        assertNotNull(result);
        assertEquals("order-1", result.getOrderId());
    }

    @Test
    void getOrderById_notFound() {
        when(orderRepository.findById("non-existent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            orderService.getOrderById("non-existent");
        });

        assertEquals(ErrorCode.ORDER_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    void getOrdersByUser_success() {
        Order order1 = Order.builder()
                .id("order-1")
                .user(testUser)
                .status(OrderStatus.PENDING)
                .totalAmount(200000)
                .build();
        Order order2 = Order.builder()
                .id("order-2")
                .user(testUser)
                .status(OrderStatus.DELIVERED)
                .totalAmount(150000)
                .build();

        OrderResponse response1 = OrderResponse.builder()
                .orderId("order-1")
                .totalAmount(200000)
                .build();
        OrderResponse response2 = OrderResponse.builder()
                .orderId("order-2")
                .totalAmount(150000)
                .build();

        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
        when(orderRepository.findAllByUser(testUser)).thenReturn(List.of(order1, order2));
        when(orderMapper.toOrderResponse(order1)).thenReturn(response1);
        when(orderMapper.toOrderResponse(order2)).thenReturn(response2);

        List<OrderResponse> results = orderService.getOrdersByUser("user-1");

        assertNotNull(results);
        assertEquals(2, results.size());
    }

    @Test
    void getOrdersByUser_userNotExist() {
        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            orderService.getOrdersByUser("non-existent");
        });

        assertEquals(ErrorCode.USER_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    @SuppressWarnings("null")
    void updateOrderStatus_success() {
        Order order = Order.builder()
                .id("order-1")
                .user(testUser)
                .status(OrderStatus.PENDING)
                .totalAmount(200000)
                .build();

        OrderResponse response = OrderResponse.builder()
                .orderId("order-1")
                .totalAmount(200000)
                .build();

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0, Order.class);
            return savedOrder;
        });
        when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(response);

        OrderResponse result = orderService.updateOrderStatus("order-1", OrderStatus.DELIVERED);

        assertNotNull(result);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @SuppressWarnings("null")
    void updateOrderStatus_cancelled() {
        Shipment shipment = Shipment.builder()
                .id("ship-1")
                .status("PREPARING")
                .build();

        Order order = Order.builder()
                .id("order-1")
                .user(testUser)
                .status(OrderStatus.PENDING)
                .totalAmount(200000)
                .shipment(shipment)
                .build();

        OrderResponse response = OrderResponse.builder()
                .orderId("order-1")
                .totalAmount(200000)
                .build();

        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0, Order.class);
            return savedOrder;
        });
        when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(response);

        OrderResponse result = orderService.updateOrderStatus("order-1", OrderStatus.CANCELLED);

        assertNotNull(result);
        assertEquals("CANCELLED", order.getShipment().getStatus());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_orderNotExist() {
        when(orderRepository.findById("non-existent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            orderService.updateOrderStatus("non-existent", OrderStatus.DELIVERED);
        });

        assertEquals(ErrorCode.ORDER_NOT_EXIST, exception.getErrorCode());
    }
}
