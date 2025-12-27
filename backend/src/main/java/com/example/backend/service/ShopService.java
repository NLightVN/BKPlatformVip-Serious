package com.example.backend.service;

import com.example.backend.dto.response.ShopResponse;
import com.example.backend.dto.request.ShopCreationRequest;
import com.example.backend.entity.Shop;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.mapper.ShopMapper;
import com.example.backend.repository.ShopRepository;
import com.example.backend.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ShopService {
    ShopRepository shopRepository;
    ShopMapper shopMapper;
    UserRepository userRepository;

    public ShopResponse createShop(ShopCreationRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        var user = userRepository.findByUsername(username).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        Shop shop = shopMapper.toShop(request);
        shop.setOwner(user);

        Shop savedShop = shopRepository.save(shop);
        return shopMapper.toShopResponse(savedShop);
    }

    public List<ShopResponse> getAllShop() {
        return shopRepository.findAll().stream().map(shopMapper::toShopResponse).toList();
    }

    public List<ShopResponse> getShopsByDistrict(String district) {
        return shopRepository.findAllByAddress_District(district).stream().map(shopMapper::toShopResponse).toList();
    }

    public List<ShopResponse> getShopsByProvince(String province) {
        return shopRepository.findAllByAddress_Province(province).stream().map(shopMapper::toShopResponse).toList();
    }

    public List<ShopResponse> getShopsByDistrictAndProvince(String district, String province) {
        return shopRepository.findAllByAddress_DistrictAndAddress_Province(district, province).stream().map(shopMapper::toShopResponse).toList();
    }

    public ShopResponse getShopById(String shopId) {
        return shopMapper.toShopResponse(shopRepository.findById(shopId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOP_NOT_EXIST)));
    }

    public List<ShopResponse> getAllShopByOwnerId(String ownerUsername) {
        var owner = userRepository.findByUsername(ownerUsername).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));
        log.info(owner.getUsername());
        return shopRepository.findAllByOwner(owner).stream().map(shopMapper::toShopResponse).toList();
    }

}
