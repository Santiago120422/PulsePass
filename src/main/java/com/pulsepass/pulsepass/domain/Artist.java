package com.pulsepass.pulsepass.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "artists")
@Getter @Setter @Builder
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "stage_name", nullable = false, unique = true)
    private String stageName;

    private String country;
    private String genre;

    @Column(nullable = false)
    private Boolean active;

    @ManyToMany(mappedBy = "artists")
    @Builder.Default
    private Set<Event> events = new HashSet<>();

    // Constructor vacío requerido por JPA
    public Artist() {
    }

    // Constructor completo (necesario para que @Builder funcione correctamente)
    public Artist(Long id, String stageName, String country, String genre, Boolean active, Set<Event> events) {
        this.id = id;
        this.stageName = stageName;
        this.country = country;
        this.genre = genre;
        this.active = active;
        this.events = events;
    }
}