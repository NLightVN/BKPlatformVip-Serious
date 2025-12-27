package com.example.backend.dto.request;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDTO {
    String phone;
    String name;
    String addressDetail;
    String wardCode;
}