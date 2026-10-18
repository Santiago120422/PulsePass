package com.pulsepass.pulsepass.domain;

import com.pulsepass.pulsepass.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Embeddable
@Getter @Setter @EqualsAndHashCode
public class UserRoleId implements Serializable {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;

    // Constructor vacío requerido por JPA
    public UserRoleId() {
    }

    // Constructor completo con todos los argumentos
    public UserRoleId(Long userId, Role role) {
        this.userId = userId;
        this.role = role;
    }
}