package com.example.backend.mapper;

import com.example.backend.dto.request.ProductCreationRequest;
import com.example.backend.dto.response.ProductResponse;
import com.example.backend.entity.Product;
import com.example.backend.entity.Shop;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-03T15:55:47+0700",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class ProductMapperImpl implements ProductMapper {

    @Override
    public Product toProduct(ProductCreationRequest request) {
        if ( request == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.name( request.getName() );
        product.price( request.getPrice() );
        product.brand( request.getBrand() );
        product.description( request.getDescription() );

        return product.build();
    }

    @Override
    public ProductResponse toProductResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductResponse.ProductResponseBuilder productResponse = ProductResponse.builder();

        productResponse.shopId( productShopShopId( product ) );
        productResponse.productId( product.getProductId() );
        productResponse.name( product.getName() );
        productResponse.price( product.getPrice() );
        productResponse.brand( product.getBrand() );
        productResponse.description( product.getDescription() );

        productResponse.categories( mapCategories(product) );
        productResponse.images( mapImages(product) );

        return productResponse.build();
    }

    @Override
    public void updateProduct(ProductCreationRequest request, Product product) {
        if ( request == null ) {
            return;
        }

        product.setName( request.getName() );
        product.setPrice( request.getPrice() );
        product.setBrand( request.getBrand() );
        product.setDescription( request.getDescription() );
    }

    private String productShopShopId(Product product) {
        Shop shop = product.getShop();
        if ( shop == null ) {
            return null;
        }
        return shop.getShopId();
    }
}
