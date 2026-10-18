package com.pulsepass.pulsepass.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/** Identidad comprobada: ID persistente + authorities cargadas desde la base de datos. */
public class SecurityPrincipal extends User {

    private final Long id;

    public SecurityPrincipal(Long id, String username, String passwordHash, boolean enabled,
                             Collection<? extends GrantedAuthority> authorities) {
        super(username, passwordHash, enabled, true, true, true, authorities);
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
