package com.example.backend.service;

import com.example.backend.constant.PredefinedRole;
import com.example.backend.dto.request.AddressDTO;
import com.example.backend.dto.request.UserCreationRequest;
import com.example.backend.dto.request.UserUpdateRequest;
import com.example.backend.dto.response.UserResponse;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.mapper.UserMapper;
import com.example.backend.repository.RoleRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    RoleRepository roleRepository;

    @InjectMocks
    UserService userService;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = Role.builder()
                .name(PredefinedRole.USER_ROLE)
                .description("User role")
                .build();

        testUser = User.builder()
                .userId("user-1")
                .username("testuser")
                .email("test@sis.hust.edu.vn")
                .fullname("Test User")
                .password("encoded-password")
                .roles(new HashSet<>(Set.of(userRole)))
                .createdDate(LocalDateTime.now())
                .build();
    }

    @Test
    @SuppressWarnings("null")
    void createUser_success() {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("newuser")
                .password("12345678")
                .fullname("New User")
                .email("newuser@sis.hust.edu.vn")
                .address(AddressDTO.builder()
                        .name("Test")
                        .phone("0123456789")
                        .addressDetail("HN")
                        .wardCode("001")
                        .build())
                .build();

        User newUser = User.builder()
                .username("newuser")
                .email("newuser@sis.hust.edu.vn")
                .password("12345678")  // Set password để mapper trả về user có password
                .build();
        assertNotNull(newUser);

        UserResponse response = UserResponse.builder()
                .userId("user-2")
                .username("newuser")
                .email("newuser@sis.hust.edu.vn")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@sis.hust.edu.vn")).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(newUser);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(roleRepository.findById(PredefinedRole.USER_ROLE)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any())).thenReturn(newUser);
        when(userMapper.toUserResponse(any())).thenReturn(response);

        UserResponse result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userRepository).save(any());
    }

    @Test
    void createUser_usernameExisted() {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("existinguser")
                .password("12345678")
                .email("new@sis.hust.edu.vn")
                .build();

        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> {
            userService.createUser(request);
        });

        assertEquals(ErrorCode.USERNAME_EXISTED, exception.getErrorCode());
    }

    @Test
    void createUser_invalidEmail() {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("newuser")
                .password("12345678")
                .email("invalid@gmail.com")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> {
            userService.createUser(request);
        });

        assertEquals(ErrorCode.INVALID_EMAIL, exception.getErrorCode());
    }

    @Test
    void createUser_emailExisted() {
        UserCreationRequest request = UserCreationRequest.builder()
                .username("newuser")
                .password("12345678")
                .email("existing@sis.hust.edu.vn")
                .build();

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@sis.hust.edu.vn")).thenReturn(true);

        AppException exception = assertThrows(AppException.class, () -> {
            userService.createUser(request);
        });

        assertEquals(ErrorCode.EMAIL_EXISTED, exception.getErrorCode());
    }

    @Test
    void getUserById_success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::requireAdmin).thenAnswer(invocation -> null);

            UserResponse response = UserResponse.builder()
                    .userId("user-1")
                    .username("testuser")
                    .build();

            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            when(userMapper.toUserResponse(testUser)).thenReturn(response);

            UserResponse result = userService.getUserById("user-1");

            assertNotNull(result);
            assertEquals("testuser", result.getUsername());
        }
    }

    @Test
    void getUserById_notFound() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::requireAdmin).thenAnswer(invocation -> null);

            when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class, () -> {
                userService.getUserById("non-existent");
            });

            assertEquals(ErrorCode.USER_NOT_EXIST, exception.getErrorCode());
        }
    }

    @Test
    @SuppressWarnings("null")
    void updateUser_success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::getCurrentUsername).thenReturn("testuser");
            mockedSecurityUtil.when(() -> SecurityUtil.hasRole("ADMIN")).thenReturn(false);

            UserUpdateRequest request = UserUpdateRequest.builder()
                    .fullname("Updated Name")
                    .email("updated@sis.hust.edu.vn")
                    .build();

            UserResponse response = UserResponse.builder()
                    .userId("user-1")
                    .fullname("Updated Name")
                    .build();

            assertNotNull(testUser);
            when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
            doNothing().when(userMapper).updateUser(testUser, request);
            when(userRepository.save(any())).thenReturn(testUser);
            when(userMapper.toUserResponse(any())).thenReturn(response);

            UserResponse result = userService.updateUser("user-1", request);

            assertNotNull(result);
            verify(userRepository).save(any());
        }
    }

    @Test
    void deleteUser_success() {
        try (MockedStatic<SecurityUtil> mockedSecurityUtil = mockStatic(SecurityUtil.class)) {
            mockedSecurityUtil.when(SecurityUtil::requireAdmin).thenAnswer(invocation -> null);

            doNothing().when(userRepository).deleteById("user-1");

            userService.deleteUser("user-1");

            verify(userRepository).deleteById("user-1");
        }
    }
}
