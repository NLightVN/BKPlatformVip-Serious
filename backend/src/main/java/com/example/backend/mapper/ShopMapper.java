package com.example.backend.mapper;

import com.example.backend.dto.response.ShopResponse;
import com.example.backend.dto.request.ShopCreationRequest;
import com.example.backend.entity.Shop;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// QUAN TRỌNG: Thêm uses = {AddressMapper.class}
@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface ShopMapper {

    @Mapping(target = "ownerId", source = "owner.userId")
    @Mapping(target = "ownerUsername", source = "owner.username")
    ShopResponse toShopResponse(Shop shop);

    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "shopId", ignore = true)
    @Mapping(target = "ratings", ignore = true)
    @Mapping(target = "products", ignore = true)
    Shop toShop(ShopCreationRequest shopCreationRequest);
}