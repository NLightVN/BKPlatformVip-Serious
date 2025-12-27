package com.example.backend.dto.request;
import lombok.Data;
import java.util.Set;

@Data
public class ProductCreationRequest {
    String shopId;
    String name;
    double price;
    double weight; // Mới
    String brand;
    String description;
    Set<String> categoryNames;
}