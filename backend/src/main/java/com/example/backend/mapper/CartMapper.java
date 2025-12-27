package com.example.backend.mapper;

import com.example.backend.dto.response.CartItemResponse;
import com.example.backend.dto.response.CartResponse;
import com.example.backend.entity.Cart;
import com.example.backend.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping; //

@Mapper(componentModel = "spring")
public interface CartMapper {
    CartResponse toCartResponse(Cart cart);
    // MapStruct sẽ dùng method này để chuyển đổi từng item trong list
    @Mapping(source = "product.productId", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    CartItemResponse toCartItemResponse(CartItem cartItem);
}