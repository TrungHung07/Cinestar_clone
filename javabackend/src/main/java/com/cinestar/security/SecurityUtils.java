package com.cinestar.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/** Tien ich lay thong tin user hien tai tu SecurityContext */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** ID cua user dang dang nhap, null neu chua xac thuc */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails details) {
            return details.getId();
        }
        return null;
    }
}
