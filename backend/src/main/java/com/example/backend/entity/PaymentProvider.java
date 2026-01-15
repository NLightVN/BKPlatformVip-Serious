package com.example.backend.entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity @Table(name = "payment_providers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentProvider {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
    String apiUrl;
    String secretKey;
    boolean isActive;
}