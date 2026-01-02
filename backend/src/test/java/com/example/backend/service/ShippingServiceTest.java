package com.example.backend.service;

import com.example.backend.dto.request.ShippingFeeRequest;
import com.example.backend.dto.response.ShippingFeeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpMethod;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

    @Mock
    RestTemplate restTemplate;

    @InjectMocks
    ShippingService shippingService;

    private ShippingFeeRequest shippingFeeRequest;

    @BeforeEach
    void setUp() {
        shippingFeeRequest = new ShippingFeeRequest();
        shippingFeeRequest.setFromDistrictCode("1442");
        shippingFeeRequest.setToDistrictCode("1452");
        shippingFeeRequest.setToWardCode("12345");
        shippingFeeRequest.setWeightGram(1000);
    }

    @Test
    @SuppressWarnings({"null", "unchecked"})
    void calculateShippingFee_success() {
        Map<String, Object> responseBody = new HashMap<>();
        Map<String, Object> data = new HashMap<>();
        data.put("total", "30000.0");
        responseBody.put("data", data);

        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> rawResponseEntity = (ResponseEntity<Map>) (ResponseEntity<?>) responseEntity;
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(org.springframework.http.HttpEntity.class),
                eq(Map.class)
        )).thenReturn(rawResponseEntity);

        ShippingFeeResponse result = shippingService.calculateShippingFee(shippingFeeRequest);

        assertNotNull(result);
        assertEquals(30000.0, result.getFee());
        assertEquals(3, result.getEstimatedDays());
        assertEquals("GHN Express", result.getProvider());

        verify(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(org.springframework.http.HttpEntity.class),
                eq(Map.class)
        );
    }

    @Test
    @SuppressWarnings({"null", "unchecked"})
    void calculateShippingFee_nullResponseBody() {
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(null, HttpStatus.OK);

        @SuppressWarnings("rawtypes")
        ResponseEntity<Map> rawResponseEntity = (ResponseEntity<Map>) (ResponseEntity<?>) responseEntity;
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(org.springframework.http.HttpEntity.class),
                eq(Map.class)
        )).thenReturn(rawResponseEntity);

        ShippingFeeResponse result = shippingService.calculateShippingFee(shippingFeeRequest);

        assertNotNull(result);
        assertEquals(30000.0, result.getFee()); // Fallback fee
        assertEquals(3, result.getEstimatedDays());
        assertEquals("GHN (Fallback)", result.getProvider());
    }

    @Test
    @SuppressWarnings("null")
    void calculateShippingFee_httpClientError() {
        HttpClientErrorException exception = new HttpClientErrorException(
                HttpStatus.BAD_REQUEST,
                "Bad Request"
        );

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(org.springframework.http.HttpEntity.class),
                eq(Map.class)
        )).thenThrow(exception);

        ShippingFeeResponse result = shippingService.calculateShippingFee(shippingFeeRequest);

        assertNotNull(result);
        assertEquals(30000.0, result.getFee()); // Fallback fee
        assertEquals(3, result.getEstimatedDays());
        assertEquals("GHN (Fallback)", result.getProvider());
    }

    @Test
    @SuppressWarnings("null")
    void calculateShippingFee_generalException() {
        RuntimeException exception = new RuntimeException("Network error");

        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(org.springframework.http.HttpEntity.class),
                eq(Map.class)
        )).thenThrow(exception);

        ShippingFeeResponse result = shippingService.calculateShippingFee(shippingFeeRequest);

        assertNotNull(result);
        assertEquals(30000.0, result.getFee()); // Fallback fee
        assertEquals(3, result.getEstimatedDays());
        assertEquals("GHN (Fallback)", result.getProvider());
    }

    @Test
    void createShippingOrder_success() {
        String orderId = "order-123";
        String result = shippingService.createShippingOrder(orderId);

        assertNotNull(result);
        assertTrue(result.startsWith("GHN_ORDER_"));
    }
}
