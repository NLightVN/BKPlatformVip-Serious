package com.example.backend.dto.response;

import com.example.backend.dto.request.AddressDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;



@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShopResponse {
    String shopId;
    String name;
    String ownerId; // id của user sở hữu shop
    String ownerUsername;

    AddressDTO address;
}