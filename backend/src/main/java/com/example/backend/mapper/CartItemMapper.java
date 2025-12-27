package com.example.backend.mapper;

import com.example.backend.dto.request.CartRequest;
import com.example.backend.dto.response.CartResponse;
import com.example.backend.entity.CartItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    CartResponse toCartResponse(CartItem cartItem);
}
