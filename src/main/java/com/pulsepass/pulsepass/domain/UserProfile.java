package com.pulsepass.pulsepass.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "user_profiles")
@Getter @Setter @Builder
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    private String phone;
    private String city;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // Constructor vacío requerido por JPA
    public UserProfile() {
    }

    // Constructor completo necesario para el funcionamiento del @Builder
    public UserProfile(Long id, String firstName, String lastName, String phone, String city, LocalDate birthDate, User user) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.city = city;
        this.birthDate = birthDate;
        this.user = user;
    }
}