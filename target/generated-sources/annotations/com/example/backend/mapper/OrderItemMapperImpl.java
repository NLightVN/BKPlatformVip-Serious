package com.example.backend.mapper;

import com.example.backend.dto.response.OrderItemResponse;
import com.example.backend.entity.OrderItem;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-03T15:55:48+0700",
    comments = "version: 1.6.2, compiler: javac, environment: Java 21.0.9 (Microsoft)"
)
@Component
public class OrderItemMapperImpl implements OrderItemMapper {

    @Override
    public OrderItemResponse toOrderItemResponse(OrderItem orderItem) {
        if ( orderItem == null ) {
            return null;
        }

        OrderItemResponse.OrderItemResponseBuilder orderItemResponse = OrderItemResponse.builder();

        orderItemResponse.quantity( orderItem.getQuantity() );
        orderItemResponse.priceAtPurchase( orderItem.getPriceAtPurchase() );

        return orderItemResponse.build();
    }
}
