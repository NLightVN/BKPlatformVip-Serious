package com.example.backend.mapper;


import com.example.backend.dto.request.ProductCreationRequest;
import com.example.backend.dto.response.CategoryResponse;
import com.example.backend.dto.response.ProductImageResponse;
import com.example.backend.dto.response.ProductResponse;
import com.example.backend.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    Product toProduct(ProductCreationRequest request);

    @Mapping(target = "shopId", source = "shop.shopId")
    @Mapping(target = "categories", expression = "java(mapCategories(product))")
    @Mapping(target = "images", expression = "java(mapImages(product))")
    ProductResponse toProductResponse(Product product);

    // vì có 3 attr phức tạp là shop, image, category nên cần map thủ công
    // ------- Custom mapping: Category -> Set<String> -------
    default Set<CategoryResponse> mapCategories(Product product) {
        if (product.getCategories() == null) return null;

        return product.getCategories().stream()
                .map(c -> CategoryResponse.builder()
                        .categoryId(c.getCategoryId())
                        .name(c.getName())
                        .build())
                .collect(Collectors.toSet());
    }

    // ------- Custom mapping: ProductImage -> ProductImageResponse -------
    default Set<ProductImageResponse> mapImages(Product product) {
        if (product.getImages() == null) return null;

        return product.getImages().stream()
                .map(img -> ProductImageResponse.builder()
                        .imageType(img.getImageType())
                        .imageUrl(img.getImageUrl())
                        .description(img.getDescription())
                        .build()
                )
                .collect(Collectors.toSet());
    }

    @Mapping(target = "shop", ignore = true)         // giữ nguyên shop
    @Mapping(target = "categories", ignore = true)   // set categories riêng
    @Mapping(target = "images", ignore = true)       // set images riêng
    void updateProduct(ProductCreationRequest request, @MappingTarget Product product);
}
