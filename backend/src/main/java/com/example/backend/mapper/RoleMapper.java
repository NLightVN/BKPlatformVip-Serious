package com.example.backend.mapper;

import com.example.backend.dto.response.RoleResponse;
import com.example.backend.dto.request.RoleRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.example.backend.entity.Role;
@Mapper(componentModel = "spring")
public interface RoleMapper {
    // sẽ ignore set permission vì khi request nó là set của string còn trong class Role thì nó là set của permission
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}
