package com.example.backend.repository;

import com.example.backend.entity.Order;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {
    List<Order> findAllByUserOrderByCreatedAtDesc(User user);
    
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.shipment JOIN o.items i JOIN i.product p WHERE p.shop.shopId = :shopId ORDER BY o.createdAt DESC")
    List<Order> findAllByShopId(@org.springframework.data.repository.query.Param("shopId") String shopId);
}
