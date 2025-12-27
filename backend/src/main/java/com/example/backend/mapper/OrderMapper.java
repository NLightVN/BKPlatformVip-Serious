package com.example.backend.mapper;


import com.example.backend.dto.request.OrderCreationRequest;
import com.example.backend.dto.response.OrderResponse;
import com.example.backend.entity.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    public OrderResponse toOrderResponse(Order order);

}
