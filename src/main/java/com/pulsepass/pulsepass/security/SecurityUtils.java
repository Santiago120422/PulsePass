package com.pulsepass.pulsepass.security;

import org.springframework.security.core.Authentication;

public final class SecurityUtils {

    private SecurityUtils() {}

    public static boolean hasAnyRole(Authentication authentication, String... roles) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        for (String role : roles) {
            for (var authority : authentication.getAuthorities()) {
                if (authority.getAuthority().equals("ROLE_" + role)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isStaffOrAdmin(Authentication authentication) {
        return hasAnyRole(authentication, "STAFF", "ADMIN");
    }

    /** Devuelve el principal propio de la app, o null si la autenticacion no es de este tipo. */
    public static SecurityPrincipal principalOf(Authentication authentication) {
        return (authentication != null && authentication.getPrincipal() instanceof SecurityPrincipal p) ? p : null;
    }
}
