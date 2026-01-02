package com.example.backend.service;

import com.example.backend.entity.PasswordResetToken;
import com.example.backend.entity.User;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class PasswordRecoveryService {

    UserRepository userRepository;
    PasswordResetTokenRepository resetTokenRepository;
    EmailService emailService;
    PasswordEncoder passwordEncoder;

    @PersistenceContext
    EntityManager entityManager;

    // 1. Gửi mail reset
    @Transactional
    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        //xoa token cu va flush
        resetTokenRepository.findByUser(user).ifPresent(token -> {
            resetTokenRepository.delete(token);
            resetTokenRepository.flush(); 
        });

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .build();
        resetTokenRepository.save(resetToken);

        String resetLink = "http://localhost:3000/reset-password?token=" + token;
        String subject = "Password Reset Request";
        String body = "Xin chào " + user.getUsername() + ",\n\n"
                + "Vui lòng nhấn link sau để đổi mật khẩu (hết hạn sau 30 phút):\n"
                + resetLink + "\n\nThân,\nBK Platform Team";

        try {
            emailService.sendSimpleMail(user.getEmail(), subject, body);
        } catch (RuntimeException e) {
            log.error("Cannot send password reset email: {}", e.getMessage());
            throw new AppException(ErrorCode.EMAIL_SEND_FAILED);
        }
    }


    // 2. Thực hiện reset password
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_TOKEN));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.TOKEN_EXPIRED);
        }

        // MUST: load user from repository (managed entity)
        User user = userRepository.findById(resetToken.getUser().getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        String newPasswordEncoded = passwordEncoder.encode(newPassword);

        user.setPassword(newPasswordEncoded);

        userRepository.saveAndFlush(user);

        entityManager.clear();
        // verify from DB
        User reloaded = userRepository.findById(user.getUserId()).get();

        log.info(">>> HASH IN DB: {}", reloaded.getPassword());
        log.info(">>> MATCH DB: {}", passwordEncoder.matches(newPassword, reloaded.getPassword()));

        resetTokenRepository.delete(resetToken);
    }
    //OK

}

