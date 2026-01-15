package com.example.backend.mapper;

import com.example.backend.dto.response.ShipmentResponse;
import com.example.backend.entity.Shipment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {
    ShipmentResponse toShipmentResponse(Shipment shipment);
}
