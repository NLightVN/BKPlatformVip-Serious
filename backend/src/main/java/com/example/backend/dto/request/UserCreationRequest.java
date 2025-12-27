package com.example.backend.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {

    String username;

    @Size(min = 8, message = "Password must be at least 8 characters")
    String password;

    String fullname;

    String email;

    AddressDTO address;
}
