package com.example.backend.dto.response;

import com.example.backend.dto.request.AddressDTO;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    String userId;
    String username;
    String fullname;
    String email;
    Set<RoleResponse> roles;
    LocalDateTime createdDate;
    AddressDTO address;

}
