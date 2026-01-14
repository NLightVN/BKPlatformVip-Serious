package com.example.backend.service;

import com.example.backend.dto.response.RoleResponse;
import com.example.backend.dto.request.RoleRequest;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.mapper.RoleMapper;
import com.example.backend.repository.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

import static com.example.backend.util.SecurityUtil.requireAdmin;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleService {
    RoleRepository roleRepository;
    RoleMapper roleMapper;

    public RoleResponse create(RoleRequest request) {
        requireAdmin();
        if (roleRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.ROLE_EXISTED);
        }

        var role = roleMapper.toRole(request);
        // dùng findById nếu nhận vào 1 phần tử
        // dùng findAllById khi nhận  vào 1 Iterable

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }


    public List<RoleResponse> getAll() {
        requireAdmin();
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }


    public void delete(String role) {
        requireAdmin();
        roleRepository.deleteById(role);
    }
}
