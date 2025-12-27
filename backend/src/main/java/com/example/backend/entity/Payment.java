package com.example.backend.entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDateTime;

@Entity @Table(name = "payments")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @OneToOne @JoinColumn(name = "order_id")
    Order order;

    @ManyToOne @JoinColumn(name = "payment_provider_id")
    PaymentProvider paymentProvider;

    String paymentMethod;
    String paymentStatus;
    double amount;
    String transactionId;

    LocalDateTime paymentDate;
}