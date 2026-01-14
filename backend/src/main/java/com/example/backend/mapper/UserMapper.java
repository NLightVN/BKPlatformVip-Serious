package com.example.backend.mapper;

import com.example.backend.dto.response.UserResponse;
import com.example.backend.dto.request.UserCreationRequest;
import com.example.backend.dto.request.UserUpdateRequest;
import com.example.backend.entity.User;
import org.mapstruct.*;

// QUAN TRỌNG: Thêm uses = {AddressMapper.class} để map được Ward
@Mapper(componentModel = "spring", uses = { AddressMapper.class, RoleMapper.class })
public interface UserMapper {

    @Mapping(target = "roles", ignore = true) // Roles xử lý riêng trong Service
    User toUser(UserCreationRequest user);

    @Mapping(target = "roleNames", expression = "java(user.getRoles() != null ? user.getRoles().stream().map(r -> r.getName()).collect(java.util.stream.Collectors.toList()) : null)")
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "address", source = "address")
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);
}