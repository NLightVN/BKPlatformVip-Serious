package com.example.backend.dto.request;
import lombok.Data;
import java.util.List;

@Data
public class OrderSelectedItemsRequest {
    List<String> productIds;   // chọn trong cart
}
