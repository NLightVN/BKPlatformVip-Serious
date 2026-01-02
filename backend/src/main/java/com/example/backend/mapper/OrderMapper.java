package com.example.backend.mapper;


import com.example.backend.dto.request.OrderCreationRequest;
import com.example.backend.dto.response.OrderResponse;
import com.example.backend.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {
    @Mapping(target = "orderId", source = "id")
    OrderResponse toOrderResponse(Order order);
}

