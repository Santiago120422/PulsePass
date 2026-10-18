package com.pulsepass.pulsepass.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** Credencial 1:1 con User. Vive aparte para que el hash NUNCA llegue a un DTO por MapStruct. */
@Entity
@Table(name = "user_credentials")
@Getter @Setter
public class UserCredential {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    // Constructor vacío requerido por JPA
    public UserCredential() {
    }

    // Constructor con todos los argumentos
    public UserCredential(Long userId, String passwordHash, LocalDateTime passwordChangedAt) {
        this.userId = userId;
        this.passwordHash = passwordHash;
        this.passwordChangedAt = passwordChangedAt;
    }
}