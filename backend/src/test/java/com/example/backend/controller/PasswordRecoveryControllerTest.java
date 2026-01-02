package com.example.backend.controller;

import com.example.backend.configuration.CustomJwtDecoder;
import com.example.backend.configuration.SecurityConfig;
import com.example.backend.exception.GlobalExceptionHandler;
import com.example.backend.service.PasswordRecoveryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PasswordRecoveryController.class)
@Import(SecurityConfig.class)
class PasswordRecoveryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    private CustomJwtDecoder customJwtDecoder;

    @MockitoBean
    PasswordRecoveryService recoveryService;

    @Test
    void forgotPassword_success() throws Exception {
        doNothing().when(recoveryService).sendPasswordResetEmail("test@sis.hust.edu.vn");

        mockMvc.perform(post("/auth/forgot-password")
                        .param("email", "test@sis.hust.edu.vn"))
                .andExpect(status().isOk())
                .andExpect(content().string("✅ Check your email for reset link!"));
    }

    @Test
    void resetPassword_success() throws Exception {
        doNothing().when(recoveryService).resetPassword(eq("valid-token"), eq("newPassword123"));

        mockMvc.perform(post("/auth/reset-password")
                        .param("token", "valid-token")
                        .param("newPassword", "newPassword123"))
                .andExpect(status().isOk())
                .andExpect(content().string("✅ Password reset successfully!"));
    }
}
