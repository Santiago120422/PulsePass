package com.pulsepass.pulsepass.service;

import com.pulsepass.pulsepass.repository.TicketRepository;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.security.SecurityPrincipal;
import com.pulsepass.pulsepass.security.SecurityUtils;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Politicas de propiedad (anti-IDOR). Se invoca desde @PreAuthorize("@authz.metodo(...)").
 * Nunca confia en un ID/email enviado por el cliente: compara contra el principal (ID persistente).
 * Politica unica para recurso ajeno o inexistente: 403 (no revela si el ticket existe).
 */
@Service("authz")
@Transactional(readOnly = true)
public class AuthorizationService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public AuthorizationService(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    private boolean ownsTicket(String ticketCode, SecurityPrincipal principal) {
        return ticketRepository.findByTicketCode(ticketCode)
                .map(t -> t.getUser().getId().equals(principal.getId()))
                .orElse(false);
    }

    /** FR-SECTKT-02: propietario, o STAFF/ADMIN. */
    public boolean canAccessTicket(String ticketCode, Authentication authentication) {
        SecurityPrincipal principal = SecurityUtils.principalOf(authentication);
        if (principal == null) return false;
        return SecurityUtils.isStaffOrAdmin(authentication) || ownsTicket(ticketCode, principal);
    }

    /** FR-SECTKT-05: propietario (USER) o ADMIN. STAFF no cancela tickets ajenos. */
    public boolean canCancelTicket(String ticketCode, Authentication authentication) {
        SecurityPrincipal principal = SecurityUtils.principalOf(authentication);
        if (principal == null) return false;
        return SecurityUtils.hasAnyRole(authentication, "ADMIN") || ownsTicket(ticketCode, principal);
    }

    /** FR-SECTKT-01: el email del DTO debe pertenecer al principal (se compara por su ID persistente). */
    public boolean isSelfEmail(String email, Authentication authentication) {
        SecurityPrincipal principal = SecurityUtils.principalOf(authentication);
        if (principal == null || email == null) return false;
        return userRepository.findById(principal.getId())
                .map(u -> u.getEmail().equalsIgnoreCase(email.trim()))
                .orElse(false);
    }

    /** FR-SECTKT-03 / FR-SECUSR-02: el propio email, o ADMIN. */
    public boolean canAccessEmail(String email, Authentication authentication) {
        return SecurityUtils.principalOf(authentication) != null
                && (SecurityUtils.hasAnyRole(authentication, "ADMIN") || isSelfEmail(email, authentication));
    }

    /** FR-SECUSR-03: el propio username, o ADMIN. */
    public boolean canAccessUsername(String username, Authentication authentication) {
        SecurityPrincipal principal = SecurityUtils.principalOf(authentication);
        if (principal == null || username == null) return false;
        return SecurityUtils.hasAnyRole(authentication, "ADMIN")
                || userRepository.findById(principal.getId())
                        .map(u -> u.getUsername().equals(username))
                        .orElse(false);
    }
}
