package com.example.backend.service;

import com.example.backend.dto.request.PaymentRequest;
import com.example.backend.dto.response.PaymentResponse;
import com.example.backend.entity.Payment;
import com.example.backend.enums.OrderStatus;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.OrderRepository;
import com.example.backend.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor // Lombok tự tạo constructor cho các field final
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true) // Tự động thêm private final
@Slf4j // Lombok tự tạo biến log
public class PaymentSimulationService {

    PaymentRepository paymentRepository;
    OrderRepository orderRepository;

    /**
     * Giả lập thanh toán online (VNPay, Momo, ...)
     * Trong thực tế sẽ redirect đến cổng thanh toán
     */
    @Transactional
    public PaymentResponse simulateOnlinePayment(PaymentRequest request) {
        var order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));

        // Giả lập xử lý payment
        boolean isSuccess = Math.random() > 0.1; // 90% thành công

        String transactionId = "TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.getDefault());
        String status = isSuccess ? "SUCCESS" : "FAILED";

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(status)
                .amount(order.getTotalAmount())
                .transactionId(transactionId)
                .paymentDate(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        // Cập nhật order status
        if (isSuccess) {
            order.setStatus(OrderStatus.PAID);
            orderRepository.save(order);
        }

        log.info("Payment simulation: {} for order {}", status, order.getId());

        return PaymentResponse.builder()
                .orderId(order.getId())
                .transactionId(transactionId)
                .status(status)
                .amount(order.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(LocalDateTime.now())
                .message(isSuccess ? "Thanh toán thành công" : "Thanh toán thất bại")
                .build();
    }

    /**
     * Thanh toán COD (Ship COD - Thanh toán khi nhận hàng)
     */
    @Transactional
    public PaymentResponse createCODPayment(String orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));

        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod("COD")
                .paymentStatus("PENDING")
                .amount(order.getTotalAmount())
                .transactionId("COD_" + orderId)
                .paymentDate(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        // Order vẫn ở trạng thái PENDING, chờ ship
        log.info("COD payment created for order {}", orderId);

        return PaymentResponse.builder()
                .orderId(order.getId())
                .transactionId("COD_" + orderId)
                .status("PENDING")
                .amount(order.getTotalAmount())
                .paymentMethod("COD")
                .message("Thanh toán khi nhận hàng")
                .build();
    }

    /**
     * Admin xác nhận đã nhận tiền COD
     */
    @Transactional
    public PaymentResponse confirmCODPayment(String orderId) {
        var order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXIST));

        Payment payment = paymentRepository.findByOrder_Id(orderId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));

        payment.setPaymentStatus("SUCCESS");
        payment.setPaymentDate(LocalDateTime.now());

        order.setStatus(OrderStatus.PAID);
        orderRepository.save(order);

        log.info("COD payment confirmed for order {}", orderId);

        return PaymentResponse.builder()
                .orderId(order.getId())
                .transactionId(payment.getTransactionId())
                .status("SUCCESS")
                .amount(order.getTotalAmount())
                .paymentMethod("COD")
                .message("Đã xác nhận thanh toán COD")
                .build();
    }
}