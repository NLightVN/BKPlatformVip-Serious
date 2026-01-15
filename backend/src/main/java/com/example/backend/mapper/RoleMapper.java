package com.example.backend.mapper;

import com.example.backend.dto.response.RoleResponse;
import com.example.backend.dto.request.RoleRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.example.backend.entity.Role;
@Mapper(componentModel = "spring")
public interface RoleMapper {

    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}

