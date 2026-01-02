package com.example.backend.service;

import com.example.backend.dto.request.AuthenticationRequest;
import com.example.backend.dto.request.IntrospectRequest;
import com.example.backend.dto.request.LogoutRequest;
import com.example.backend.dto.response.AuthenticationResponse;
import com.example.backend.dto.response.IntrospectResponse;
import com.example.backend.entity.InvalidatedToken;
import com.example.backend.entity.Role;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.InvalidatedTokenRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    InvalidatedTokenRepository invalidatedTokenRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthenticationService authenticationService;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Set up test values using reflection
        ReflectionTestUtils.setField(Objects.requireNonNull(authenticationService), "SIGNER_KEY",
                "test-signer-key-for-ci-cd-pipeline-minimum-256-bits-long-key-for-testing-only");
        ReflectionTestUtils.setField(Objects.requireNonNull(authenticationService), "VALID_DURATION", 60L);
        ReflectionTestUtils.setField(Objects.requireNonNull(authenticationService), "REFRESHABLE_DURATION", 7L);

        userRole = Role.builder()
                .name("USER")
                .description("User role")
                .build();

        testUser = User.builder()
                .userId("user-1")
                .username("testuser")
                .email("test@sis.hust.edu.vn")
                .password("encoded-password")
                .roles(new HashSet<>(Set.of(userRole)))
                .build();
    }

    @Test
    void authenticate_success() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("testuser")
                .password("12345678")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("12345678", "encoded-password")).thenReturn(true);

        AuthenticationResponse result = authenticationService.authenticate(request);

        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertNotNull(result.getToken());
    }

    @Test
    void authenticate_userNotExist() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("non-existent")
                .password("12345678")
                .build();

        when(userRepository.findByUsername("non-existent")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> authenticationService.authenticate(request));

        assertEquals(ErrorCode.USER_NOT_EXIST, exception.getErrorCode());
    }

    @Test
    void authenticate_invalidPassword() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username("testuser")
                .password("wrong-password")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> authenticationService.authenticate(request));

        assertEquals(ErrorCode.UNAUTHENTICATED, exception.getErrorCode());
    }

    @Test
    @SuppressWarnings("null")
    void introspect_validToken() throws Exception {
        // Create a valid token first
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                .username("testuser")
                .password("12345678")
                .build();

        AuthenticationResponse authResponse = authenticationService.authenticate(authRequest);
        String token = authResponse.getToken();

        IntrospectRequest request = IntrospectRequest.builder()
                .token(token)
                .build();

        when(invalidatedTokenRepository.existsById(any(String.class))).thenReturn(false);

        IntrospectResponse result = authenticationService.introspect(request);

        assertNotNull(result);
        assertTrue(result.isValid());
    }

    @Test
    void introspect_invalidToken_throwsException() {
        IntrospectRequest request = IntrospectRequest.builder()
                .token("invalid-token")
                .build();

        assertThrows(ParseException.class, () ->
                authenticationService.introspect(request)
        );
    }


    @Test
    @SuppressWarnings("null")
    void logout_success() throws Exception {
        // Create a valid token first
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(true);
        when(invalidatedTokenRepository.existsById(any(String.class))).thenReturn(false);

        AuthenticationRequest authRequest = AuthenticationRequest.builder()
                .username("testuser")
                .password("12345678")
                .build();

        AuthenticationResponse authResponse = authenticationService.authenticate(authRequest);
        String token = authResponse.getToken();

        LogoutRequest request = LogoutRequest.builder()
                .token(token)
                .build();

        when(invalidatedTokenRepository.save(any(InvalidatedToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> authenticationService.logout(request));

        verify(invalidatedTokenRepository).save(any(InvalidatedToken.class));
    }
}
