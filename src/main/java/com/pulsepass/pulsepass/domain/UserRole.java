package com.pulsepass.pulsepass.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "user_roles")
@Getter @Setter
public class UserRole {

    @EmbeddedId
    private UserRoleId id;

    // Constructor vacío requerido por JPA
    public UserRole() {
    }

    // Constructor completo con todos los argumentos
    public UserRole(UserRoleId id) {
        this.id = id;
    }
}