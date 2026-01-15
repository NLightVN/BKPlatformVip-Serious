package com.example.backend.repository;

import com.example.backend.entity.Product;
import com.example.backend.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    List<Product> findAllByShop(Shop shop);
    List<Product> findAllByCategories_Name(String categoryName);
    List<Product> findAllByBrandIgnoreCase(String brand);
    Optional<Product> findById(String id);
    List<Product> findAllByShopAndBrand(Shop shop, String brand);
    List<Product> findByNameContainingIgnoreCase(String keyword);
}
