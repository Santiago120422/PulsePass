package com.pulsepass.pulsepass.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter @Setter @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Boolean active;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile profile;

    // Constructor vacío requerido por JPA
    public User() {
    }

    // Constructor completo necesario para el funcionamiento del @Builder
    public User(Long id, String username, String email, Boolean active, UserProfile profile) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.active = active;
        this.profile = profile;
    }
}