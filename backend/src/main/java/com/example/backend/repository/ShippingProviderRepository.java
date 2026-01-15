package com.example.backend.repository;

import com.example.backend.entity.ShippingProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShippingProviderRepository extends JpaRepository<ShippingProvider, String> {
    // Tìm provider theo tên
    Optional<ShippingProvider> findByName(String name);

    // List<ShippingProvider> findByIsActiveTrue();
}