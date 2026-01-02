package com.example.backend.service;

import com.example.backend.dto.request.RoleRequest;
import com.example.backend.dto.response.RoleResponse;
import com.example.backend.entity.Role;
import com.example.backend.mapper.RoleMapper;
import com.example.backend.repository.RoleRepository;
import com.example.backend.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    RoleRepository roleRepository;

    @Mock
    RoleMapper roleMapper;

    @InjectMocks
    RoleService roleService;

    private Role testRole;
    private RoleRequest roleRequest;
    private RoleResponse roleResponse;

    @BeforeEach
    void setUp() {
        testRole = Role.builder()
                .name("TEST_ROLE")
                .description("Test Role Description")
                .build();

        roleRequest = RoleRequest.builder()
                .name("TEST_ROLE")
                .description("Test Role Description")
                .build();

        roleResponse = RoleResponse.builder()
                .name("TEST_ROLE")
                .description("Test Role Description")
                .build();
    }

    @Test
    @SuppressWarnings("null")
    void create_success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::requireAdmin).thenAnswer(invocation -> null);

            when(roleMapper.toRole(any(RoleRequest.class))).thenReturn(testRole);
            when(roleRepository.save(any(Role.class))).thenAnswer(invocation -> invocation.getArgument(0, Role.class));
            when(roleMapper.toRoleResponse(any(Role.class))).thenReturn(roleResponse);

            RoleResponse result = roleService.create(roleRequest);

            assertNotNull(result);
            assertEquals("TEST_ROLE", result.getName());
            assertEquals("Test Role Description", result.getDescription());

            verify(roleMapper).toRole(any(RoleRequest.class));
            verify(roleRepository).save(any(Role.class));
            verify(roleMapper).toRoleResponse(any(Role.class));
        }
    }

    @Test
    void getAll_success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::requireAdmin).thenAnswer(invocation -> null);

            Role role2 = Role.builder()
                    .name("ADMIN")
                    .description("Admin Role")
                    .build();

            RoleResponse response2 = RoleResponse.builder()
                    .name("ADMIN")
                    .description("Admin Role")
                    .build();

            when(roleRepository.findAll()).thenReturn(List.of(testRole, role2));
            when(roleMapper.toRoleResponse(testRole)).thenReturn(roleResponse);
            when(roleMapper.toRoleResponse(role2)).thenReturn(response2);

            List<RoleResponse> results = roleService.getAll();

            assertNotNull(results);
            assertEquals(2, results.size());
            assertEquals("TEST_ROLE", results.get(0).getName());
            assertEquals("ADMIN", results.get(1).getName());

            verify(roleRepository).findAll();
            verify(roleMapper, times(2)).toRoleResponse(any(Role.class));
        }
    }

    @Test
    void delete_success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::requireAdmin).thenAnswer(invocation -> null);

            doNothing().when(roleRepository).deleteById("TEST_ROLE");

            assertDoesNotThrow(() -> {
                roleService.delete("TEST_ROLE");
            });

            verify(roleRepository).deleteById("TEST_ROLE");
        }
    }
}
