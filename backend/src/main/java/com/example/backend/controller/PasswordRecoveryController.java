package com.example.backend.controller;

import com.example.backend.service.PasswordRecoveryService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.AccessLevel;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PasswordRecoveryController {

    PasswordRecoveryService recoveryService;

    // 1. Gửi mail
    @PostMapping("/forgot-password")
    public String forgotPassword(@RequestParam String email) {
        recoveryService.sendPasswordResetEmail(email);
        return "✅ Check your email for reset link!";
    }

    // 2. Reset password
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        recoveryService.resetPassword(token, newPassword);
        return "✅ Password reset successfully!";
    }
}
