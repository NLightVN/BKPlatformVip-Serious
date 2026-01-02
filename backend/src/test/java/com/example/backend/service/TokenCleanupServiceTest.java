package com.example.backend.service;

import com.example.backend.repository.InvalidatedTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenCleanupServiceTest {

    @Mock
    InvalidatedTokenRepository invalidatedTokenRepository;

    @InjectMocks
    TokenCleanupService tokenCleanupService;

    @Test
    void cleanupExpiredTokens_success() {
        int deletedCount = 5;
        when(invalidatedTokenRepository.deleteExpiredTokens()).thenReturn(deletedCount);

        assertDoesNotThrow(() -> {
            tokenCleanupService.cleanupExpiredTokens();
        });

        verify(invalidatedTokenRepository).deleteExpiredTokens();
    }

    @Test
    void cleanupExpiredTokens_noTokensToDelete() {
        when(invalidatedTokenRepository.deleteExpiredTokens()).thenReturn(0);

        assertDoesNotThrow(() -> {
            tokenCleanupService.cleanupExpiredTokens();
        });

        verify(invalidatedTokenRepository).deleteExpiredTokens();
    }

    @Test
    void cleanupExpiredTokens_multipleTokensDeleted() {
        int deletedCount = 100;
        when(invalidatedTokenRepository.deleteExpiredTokens()).thenReturn(deletedCount);

        assertDoesNotThrow(() -> {
            tokenCleanupService.cleanupExpiredTokens();
        });

        verify(invalidatedTokenRepository).deleteExpiredTokens();
    }
}
