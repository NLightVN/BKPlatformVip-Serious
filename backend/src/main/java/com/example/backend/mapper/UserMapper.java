package com.example.backend.mapper;

import com.example.backend.dto.response.UserResponse;
import com.example.backend.dto.request.UserCreationRequest;
import com.example.backend.dto.request.UserUpdateRequest;
import com.example.backend.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

// QUAN TRỌNG: Thêm uses = {AddressMapper.class} để map được Ward
@Mapper(componentModel = "spring", uses = {AddressMapper.class, RoleMapper.class})
public interface UserMapper {

    @Mapping(target = "roles", ignore = true) // Roles xử lý riêng trong Service
    User toUser(UserCreationRequest user);

    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "username", ignore = true) // Không cho update username
    @Mapping(target = "shops", ignore = true)
    @Mapping(target = "orders", ignore = true)
    @Mapping(target = "cart", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}