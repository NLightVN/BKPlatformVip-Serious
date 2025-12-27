package com.example.backend.mapper;

import com.example.backend.dto.response.PermissionResponse;
import com.example.backend.dto.request.PermissionRequest;
import com.example.backend.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}