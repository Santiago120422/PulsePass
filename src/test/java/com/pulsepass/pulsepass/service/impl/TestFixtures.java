package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.*;
import com.pulsepass.pulsepass.domain.enums.*;

import java.math.BigDecimal;
import java.time.*;

/** Builders y reloj fijo para los unit tests de Service. */
final class TestFixtures {

    static final ZoneId ZONE = ZoneId.of("America/Bogota");
    static final LocalDateTime NOW = LocalDateTime.of(2026, 10, 2, 10, 0);
    static final Clock CLOCK = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);

    private TestFixtures() {}

    static Venue venue(String code, int capacity, boolean active) {
        return Venue.builder().id(1L).code(code).name("Marina Convention Center")
                .city("Santa Marta").address("Calle 1").capacity(capacity).active(active).build();
    }

    static Event event(String code, EventStatus status, LocalDateTime date, int minAge, Venue venue) {
        return Event.builder().id(10L).eventCode(code).name("Caribbean Music Fest 2026")
                .category(EventCategory.MUSIC).status(status).eventDate(date)
                .minimumAge(minAge).venue(venue).build();
    }

    static User user(String email, boolean active, LocalDate birthDate) {
        User user = User.builder().id(20L).username(email.split("@")[0]).email(email).active(active).build();
        user.setProfile(UserProfile.builder().firstName("Test").birthDate(birthDate).user(user).build());
        return user;
    }

    static Ticket ticket(String code, TicketStatus status, User user, Event event) {
        return Ticket.builder().id(30L).ticketCode(code).type(TicketType.VIP)
                .price(new BigDecimal("300000.00")).status(status).purchaseDate(NOW.minusDays(1))
                .user(user).event(event).build();
    }

    static Artist artist(Long id, String stageName) {
        return Artist.builder().id(id).stageName(stageName).active(true).build();
    }
}
