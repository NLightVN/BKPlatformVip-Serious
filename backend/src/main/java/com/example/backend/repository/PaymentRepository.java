package com.example.backend.repository;

import com.example.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    // Tìm payment theo Order ID
    Optional<Payment> findByOrder_Id(String orderId);

    // Tìm payment theo mã giao dịch ngân hàng (để đối soát IPN)
    Optional<Payment> findByTransactionId(String transactionId);
}