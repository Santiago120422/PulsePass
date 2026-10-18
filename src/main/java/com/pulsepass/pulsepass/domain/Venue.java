package com.pulsepass.pulsepass.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "venues")
@Getter @Setter @Builder
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private Boolean active;

    @OneToMany(mappedBy = "venue")
    @Builder.Default
    private List<Event> events = new ArrayList<>();

    // Constructor vacío requerido por JPA
    public Venue() {
    }

    // Constructor completo necesario para el funcionamiento del @Builder
    public Venue(Long id, String code, String name, String city, String address, Integer capacity, Boolean active, List<Event> events) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.city = city;
        this.address = address;
        this.capacity = capacity;
        this.active = active;
        this.events = events;
    }
}