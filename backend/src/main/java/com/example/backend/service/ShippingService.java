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

    // URL chuẩn của GHN
    private static final String GHN_URL_FEE = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";
    private static final String GHN_TOKEN = "721ba613-e787-11f0-8373-1a92d62e4dc3";
    // private static final String GHN_SHOP_ID = "6194695"; // Tạm thời comment lại để test địa chỉ động

    public ShippingFeeResponse calculateShippingFee(ShippingFeeRequest request) {
        log.info("GHN Calc: From Dist={}, Ward={} -> To Dist={}, Ward={}, Weight={}",
                request.getFromDistrictCode(), request.getFromWardCode(),
                request.getToDistrictCode(), request.getToWardCode(), request.getWeightGram());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("token", GHN_TOKEN);
            headers.setContentType(MediaType.APPLICATION_JSON);
            // headers.set("ShopId", GHN_SHOP_ID); // Bỏ dòng này để GHN không ép về kho mặc định

            Map<String, Object> body = new HashMap<>();
            body.put("service_type_id", 2); // 2: Chuẩn, 5: Nhanh (Tùy cấu hình shop)

            // Ép kiểu về Integer cho District (GHN yêu cầu Int)
            try {
                body.put("from_district_id", Integer.parseInt(request.getFromDistrictCode()));
                body.put("to_district_id", Integer.parseInt(request.getToDistrictCode()));
            } catch (NumberFormatException e) {
                log.error("Mã quận huyện phải là số: From={}, To={}", request.getFromDistrictCode(), request.getToDistrictCode());
                return fallbackFee();
            }

            // Ward code là String
            if (request.getFromWardCode() != null) body.put("from_ward_code", request.getFromWardCode());
            if (request.getToWardCode() != null) body.put("to_ward_code", request.getToWardCode());

            body.put("weight", request.getWeightGram());
            body.put("length", 20);
            body.put("width", 15);
            body.put("height", 10);
            body.put("insurance_value", 0);
            body.put("coupon", null);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(GHN_URL_FEE, HttpMethod.POST, entity, Map.class);

            if (response.getBody() != null && response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null) {
                    double totalFee = Double.parseDouble(data.get("total").toString());
                    return ShippingFeeResponse.builder()
                            .fee(totalFee)
                            .estimatedDays(3)
                            .provider("GHN Express")
                            .build();
                }
            }
            return fallbackFee();

        } catch (HttpClientErrorException e) {
            log.error("GHN Error: {}", e.getResponseBodyAsString()); // Xem lỗi chi tiết ở đây
            return fallbackFee();
        } catch (Exception e) {
            log.error("System Error: {}", e.getMessage());
            return fallbackFee();
        }
    }

    private ShippingFeeResponse fallbackFee() {
        return ShippingFeeResponse.builder()
                .fee(35000.0) // Giá mặc định nếu lỗi
                .estimatedDays(3)
                .provider("GHN (Fallback)")
                .build();
    }

    public String createShippingOrder(String orderId) {
        return "GHN_ORDER_" + System.currentTimeMillis();
    }
}