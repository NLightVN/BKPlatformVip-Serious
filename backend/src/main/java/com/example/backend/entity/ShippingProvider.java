package com.example.backend.entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity @Table(name = "shipping_providers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ShippingProvider {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String name;
    String apiUrl;
    String token;
    boolean isActive;
}