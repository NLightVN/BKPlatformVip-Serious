package com.example.backend.mapper;

import com.example.backend.dto.response.CartItemResponse;
import com.example.backend.dto.response.CartResponse;
import com.example.backend.entity.Cart;
import com.example.backend.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(source = "id", target = "id")
    CartResponse toCartResponse(Cart cart);

    @Mapping(source = "product.productId", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.price", target = "price")
    @Mapping(source = "product.shop.shopId", target = "shopId")

    CartItemResponse toCartItemResponse(CartItem cartItem);
}