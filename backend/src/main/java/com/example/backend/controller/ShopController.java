package com.example.backend.controller;

import com.example.backend.dto.response.ApiResponse;
import com.example.backend.dto.response.ShopResponse;
import com.example.backend.dto.request.ShopCreationRequest;
import com.example.backend.service.ShopService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ShopController {
    ShopService shopService;
    com.example.backend.service.OrderService orderService;

    @PostMapping
    ApiResponse createShop(@Valid @RequestBody ShopCreationRequest request){
        return ApiResponse.<ShopResponse>builder()
                .result(shopService.createShop(request))
                .build();
    }
    //OK

    @GetMapping
    public ApiResponse<List<ShopResponse>> getAllShops() {
        return ApiResponse.<List<ShopResponse>>builder()
                .result(shopService.getAllShop())
                .build();
    }
    //OK

    @GetMapping("/{shopId}")
    ShopResponse getShopById(@PathVariable String shopId) {
        return shopService.getShopById(shopId);
    }
    //OK

    @GetMapping("/owner/{ownerUsername}")
    ApiResponse<List<ShopResponse>> getShopByOwnerId(@PathVariable String ownerUsername) {
        return ApiResponse.<List<ShopResponse>>builder()
                .result(shopService.getAllShopByOwnerId(ownerUsername))
                .build();
    }
    //OK

    @GetMapping("/search")
    ApiResponse<List<ShopResponse>> searchShops(
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String province) {

        List<ShopResponse> shops;

        if(district != null && province != null) {
            shops = shopService.getShopsByDistrictAndProvince(district, province);
        } else if(district != null) {
            shops = shopService.getShopsByDistrict(district);
        } else if(province != null) {
            shops = shopService.getShopsByProvince(province);
        } else {
            shops = shopService.getAllShop();
        }

        return ApiResponse.<List<ShopResponse>>builder().result(shops).build();
    }

    @DeleteMapping("/{shopId}")
    ApiResponse<Void> deleteShop(@PathVariable String shopId) {
        shopService.deleteShop(shopId);
        return ApiResponse.<Void>builder()
                .message("Shop deleted successfully")
                .build();
    }

    @GetMapping("/{shopId}/revenue")
    ApiResponse<com.example.backend.dto.response.ShopRevenueResponse> getShopRevenue(@PathVariable String shopId) {
        return ApiResponse.<com.example.backend.dto.response.ShopRevenueResponse>builder()
                .result(orderService.getShopRevenue(shopId))
                .build();
    }

}
