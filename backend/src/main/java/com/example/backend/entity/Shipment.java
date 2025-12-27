package com.example.backend.entity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import java.time.LocalDate;

@Entity @Table(name = "shipments")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Shipment {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    @OneToOne @JoinColumn(name = "order_id")
    Order order;

    @ManyToOne @JoinColumn(name = "shipping_provider_id")
    ShippingProvider shippingProvider;

    String trackingNumber;
    double shippingFee;
    LocalDate estimatedDeliveryDate;
    String status;
}