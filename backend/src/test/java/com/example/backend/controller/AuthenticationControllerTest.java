package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.dto.request.AuthenticationRequest;
import com.example.backend.dto.request.IntrospectRequest;
import com.example.backend.dto.request.LogoutRequest;
import com.example.backend.dto.request.RefreshTokenRequest;
import com.example.backend.dto.response.AuthenticationResponse;
import com.example.backend.dto.response.IntrospectResponse;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.service.AuthenticationService;
import com.example.backend.service.RoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
class AuthenticationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    AuthenticationService authenticationService;

    @MockBean
    CustomJwtDecoder customJwtDecoder; // để SecurityConfig không lỗi

    // =================== /auth/token ===================

    @Test
    void authenticate_success() throws Exception {
        AuthenticationRequest request =
                new AuthenticationRequest("user1", "123456");

        when(authenticationService.authenticate(any()))
                .thenReturn(AuthenticationResponse.builder()
                        .token("illustration-jwt-token")
                        .authenticated(true)
                        .build());

        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.token").value("illustration-jwt-token"))
                .andExpect(jsonPath("$.result.authenticated").value(true));
    }

    @Test
    void authenticate_wrongPassword() throws Exception {
        AuthenticationRequest request =
                new AuthenticationRequest("user1", "wrong");

        when(authenticationService.authenticate(any()))
                .thenThrow(new AppException(ErrorCode.UNAUTHENTICATED));

        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value(ErrorCode.UNAUTHENTICATED.getMessage()));
    }


    @Test
    void authenticate_userNotExist() throws Exception {
        AuthenticationRequest request =
                new AuthenticationRequest("unknown", "123");

        when(authenticationService.authenticate(any()))
                .thenThrow(new AppException(ErrorCode.USER_NOT_EXIST));

        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value(ErrorCode.USER_NOT_EXIST.getMessage()));
    }


    // =================== /auth/introspect ===================

    @Test
    void introspect_validToken() throws Exception {
        IntrospectRequest request = new IntrospectRequest("valid-token");

        when(authenticationService.introspect(any()))
                .thenReturn(IntrospectResponse.builder()
                        .valid(true)
                        .build());

        mockMvc.perform(get("/auth/introspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.valid").value(true));
    }

    @Test
    void introspect_invalidToken() throws Exception {
        IntrospectRequest request = new IntrospectRequest("invalid-token");

        when(authenticationService.introspect(any()))
                .thenReturn(IntrospectResponse.builder()
                        .valid(false)
                        .build());

        mockMvc.perform(get("/auth/introspect")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.valid").value(false));
    }

    // =================== /auth/refresh ===================

    @Test
    void refreshToken_success() throws Exception {
        RefreshTokenRequest request =
                new RefreshTokenRequest("old-token");

        when(authenticationService.refreshToken(any()))
                .thenReturn(AuthenticationResponse.builder()
                        .token("new-token")
                        .authenticated(true)
                        .build());

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.token").value("new-token"))
                .andExpect(jsonPath("$.result.authenticated").value(true));
    }

    @Test
    void refreshToken_invalid() throws Exception {
        RefreshTokenRequest request =
                new RefreshTokenRequest("expired-token");

        when(authenticationService.refreshToken(any()))
                .thenThrow(new AppException(ErrorCode.UNAUTHENTICATED));

        mockMvc.perform(post("/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message")
                        .value(ErrorCode.UNAUTHENTICATED.getMessage()));
    }


    // =================== /auth/logout ===================

    @Test
    void logout_success() throws Exception {
        LogoutRequest request = new LogoutRequest("token");

        Mockito.doNothing()
                .when(authenticationService).logout(any());

        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}

