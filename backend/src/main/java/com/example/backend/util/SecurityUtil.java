package com.example.backend.util;
import com.example.backend.exception.AppException;
import com.example.backend.exception.ErrorCode;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static Authentication getAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null
                || !auth.isAuthenticated()
                || auth instanceof AnonymousAuthenticationToken) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return auth;
    }

    public static String getCurrentUsername() {
        return getAuthentication().getName();
    }

    public static boolean hasRole(String role) {
        Authentication auth = getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public static void requireAdmin() {
        if (!hasRole("ADMIN")) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }


}

