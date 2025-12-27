package com.example.backend.service;


import com.example.backend.dto.request.ShippingFeeRequest;
import com.example.backend.dto.response.ShippingFeeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingService {

    private final RestTemplate restTemplate;

    private static final String GHN_URL_FEE = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee\n";

    private static final String GHN_TOKEN = "";
    private static final String GHN_SHOP_ID = "";

    public ShippingFeeResponse calculateShippingFee(ShippingFeeRequest request) {
        log.info("Calling GHN API... From: {}, To: {}, Weight: {}",
                request.getFromDistrictCode(), request.getToDistrictCode(), request.getWeightGram());

        try {
            // Tạo Headers
            HttpHeaders headers = new HttpHeaders();
            headers.set("token", GHN_TOKEN);
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (!GHN_SHOP_ID.isEmpty()) {
                headers.set("ShopId", GHN_SHOP_ID);
            }

            //  Tạo Body
            Map<String, Object> body = new HashMap<>();

            // 2 = Hàng nhẹ/Thương mại điện tử , 5 = Hàng nặng
            body.put("service_type_id", 2);

            body.put("from_district_id", Integer.parseInt(request.getFromDistrictCode()));
            body.put("to_district_id", Integer.parseInt(request.getToDistrictCode()));
            body.put("to_ward_code", request.getToWardCode());
            body.put("weight", request.getWeightGram());
            body.put("length", 20);
            body.put("width", 15);
            body.put("height", 10);
            body.put("insurance_value", 0); // bảo hiểm
            body.put("coupon", null);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            // Gọi API
            ResponseEntity<Map> response = restTemplate.exchange(
                    GHN_URL_FEE,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );

            // Xử lý kết quả thành công
            if (response.getBody() != null && response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null) {
                    double totalFee = Double.parseDouble(data.get("total").toString());
                    log.info("GHN Calculator Success: {} VND", totalFee);

                    return ShippingFeeResponse.builder()
                            .fee(totalFee)
                            .estimatedDays(3) // Mặc định, muốn chuẩn phải gọi thêm API leadtime
                            .provider("GHN Express")
                            .build();
                }
            }

            log.warn("GHN Response body is null or missing data");
            return fallbackFee();

        }catch (HttpClientErrorException e) {

        System.err.println(">>> GHN REJECTED: " + e.getResponseBodyAsString());
        log.error("GHN API Error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
        return fallbackFee();

    } catch (Exception e) {
        System.err.println(">>> CODE ERROR: " + e.getMessage());
        e.printStackTrace(); // In chi tiết lỗi dòng nào
        return fallbackFee();
    }}


    private ShippingFeeResponse fallbackFee() {
        System.out.print("Lỗi ship");
        return ShippingFeeResponse.builder()
                .fee(30000.0) // Giá fallback nếu gọi API lỗi
                .estimatedDays(3)
                .provider("GHN (Fallback)")
                .build();
    }

    public String createShippingOrder(String orderId) {
        return "GHN_ORDER_" + System.currentTimeMillis();
    }
}