package com.example.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class ProductResponse {
    String productId;
    String name;
    double price;
    String brand;
    String description;

    String shopId;                // Id của shop
    Set<CategoryResponse> categories;       // Tên các category
    Set<ProductImageResponse> images;  // Danh sách ảnh
}
