package com.example.backend.repository;

import com.example.backend.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String> {
    // Tìm shipment theo Order ID
    Optional<Shipment> findByOrder_Id(String orderId);
}