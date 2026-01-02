package com.example.backend.service;

import com.example.backend.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {

    private final InvalidatedTokenRepository invalidatedTokenRepository;

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // mỗi 1 tiếng
    public void cleanupExpiredTokens() {
        int deleted = invalidatedTokenRepository.deleteExpiredTokens();
        if (deleted > 0) {
            log.info("🧹 Cleaned {} expired invalidated tokens", deleted);
        }
    }
}

