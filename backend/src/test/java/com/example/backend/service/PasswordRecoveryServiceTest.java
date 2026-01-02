package com.example.backend.service;

import com.example.backend.entity.PasswordResetToken;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordResetTokenRepository resetTokenRepository;

    @Mock
    EmailService emailService;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    EntityManager entityManager;

    @InjectMocks
    PasswordRecoveryService passwordRecoveryService;

    private User testUser;
    private PasswordResetToken testToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .userId("user-1")
                .username("testuser")
                .email("test@sis.hust.edu.vn")
                .password("old-encoded-password")
                .build();

        testToken = PasswordResetToken.builder()
                .token("test-token-123")
                .user(testUser)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .build();
    }

    @Test
    @SuppressWarnings("null")
    void sendPasswordResetEmail_success() {
        String email = "test@sis.hust.edu.vn";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(resetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> {
            PasswordResetToken token = (PasswordResetToken) invocation.getArgument(0);
            return Objects.requireNonNull(token);
        });
        doNothing().when(emailService).sendSimpleMail(anyString(), anyString(), anyString());

        assertDoesNotThrow(() -> {
            passwordRecoveryService.sendPasswordResetEmail(email);
        });

        verify(userRepository).findByEmail(email);
        verify(resetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).sendSimpleMail(anyString(), anyString(), anyString());
    }

    @Test
    @SuppressWarnings("null")
    void sendPasswordResetEmail_userNotExist() {
        String email = "nonexistent@sis.hust.edu.vn";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            passwordRecoveryService.sendPasswordResetEmail(email);
        });

        assertEquals(ErrorCode.USER_NOT_EXIST, exception.getErrorCode());
        verify(userRepository).findByEmail(email);
        verify(resetTokenRepository, never()).save(any());
    }

    @Test
    @SuppressWarnings("null")
    void sendPasswordResetEmail_emailSendFailed() {
        String email = "test@sis.hust.edu.vn";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(testUser));
        when(resetTokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> {
            PasswordResetToken token = (PasswordResetToken) invocation.getArgument(0);
            return Objects.requireNonNull(token);
        });
        doThrow(new RuntimeException("Email service error")).when(emailService)
                .sendSimpleMail(anyString(), anyString(), anyString());

        AppException exception = assertThrows(AppException.class, () -> {
            passwordRecoveryService.sendPasswordResetEmail(email);
        });

        assertEquals(ErrorCode.EMAIL_SEND_FAILED, exception.getErrorCode());
    }

    @Test
    @SuppressWarnings("null")
    void resetPassword_success() {
        String token = "test-token-123";
        String newPassword = "newPassword123";

        when(resetTokenRepository.findByToken(token)).thenReturn(Optional.of(testToken));
        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(newPassword)).thenReturn("new-encoded-password");
        when(passwordEncoder.matches(newPassword, "new-encoded-password")).thenReturn(true);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> {
            User user = (User) invocation.getArgument(0);
            return Objects.requireNonNull(user);
        });
        when(userRepository.findById("user-1")).thenReturn(Optional.of(testUser));
        doNothing().when(resetTokenRepository).delete(testToken);
        doNothing().when(entityManager).clear();

        assertDoesNotThrow(() -> {
            passwordRecoveryService.resetPassword(token, newPassword);
        });

        verify(resetTokenRepository).findByToken(token);
        verify(userRepository, atLeastOnce()).findById("user-1");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).saveAndFlush(any(User.class));
        verify(resetTokenRepository).delete(testToken);
    }

    @Test
    @SuppressWarnings("null")
    void resetPassword_invalidToken() {
        String token = "invalid-token";
        String newPassword = "newPassword123";

        when(resetTokenRepository.findByToken(token)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            passwordRecoveryService.resetPassword(token, newPassword);
        });

        assertEquals(ErrorCode.INVALID_TOKEN, exception.getErrorCode());
        verify(resetTokenRepository).findByToken(token);
        verify(userRepository, never()).findById(anyString());
    }

    @Test
    @SuppressWarnings("null")
    void resetPassword_tokenExpired() {
        String token = "expired-token";
        String newPassword = "newPassword123";

        PasswordResetToken expiredToken = PasswordResetToken.builder()
                .token(token)
                .user(testUser)
                .expiryDate(LocalDateTime.now().minusMinutes(1)) // Expired
                .build();

        when(resetTokenRepository.findByToken(token)).thenReturn(Optional.of(expiredToken));

        AppException exception = assertThrows(AppException.class, () -> {
            passwordRecoveryService.resetPassword(token, newPassword);
        });

        assertEquals(ErrorCode.TOKEN_EXPIRED, exception.getErrorCode());
        verify(resetTokenRepository).findByToken(token);
        verify(userRepository, never()).findById(anyString());
    }

    @Test
    void resetPassword_userNotExist() {
        String token = "test-token-123";
        String newPassword = "newPassword123";

        when(resetTokenRepository.findByToken(token)).thenReturn(Optional.of(testToken));
        when(userRepository.findById("user-1")).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> {
            passwordRecoveryService.resetPassword(token, newPassword);
        });

        assertEquals(ErrorCode.USER_NOT_EXIST, exception.getErrorCode());
        verify(resetTokenRepository).findByToken(token);
        verify(userRepository).findById("user-1");
    }
}
