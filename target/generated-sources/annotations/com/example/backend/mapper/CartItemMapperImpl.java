package com.example.backend.mapper;

import com.example.backend.dto.response.CartResponse;
import com.example.backend.entity.CartItem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-03T15:55:48+0700",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class CartItemMapperImpl implements CartItemMapper {

    @Override
    public CartResponse toCartResponse(CartItem cartItem) {
        if ( cartItem == null ) {
            return null;
        }

        CartResponse.CartResponseBuilder cartResponse = CartResponse.builder();

        return cartResponse.build();
    }
}
