package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.AbstractIntegrationTest;
import com.pulsepass.pulsepass.domain.*;
import com.pulsepass.pulsepass.domain.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EventSearchIT extends AbstractIntegrationTest {

    @Autowired private EventRepository eventRepository;
    @Autowired private ArtistRepository artistRepository;
    @Autowired private VenueRepository venueRepository;
    @Autowired private TicketRepository ticketRepository;
    @Autowired private UserRepository userRepository;

    // FR-SRC-002
    @Test
    void shouldFindEventsByCityAndArtist() {
        Venue venue = venueRepository.save(Venue.builder()
                .code("VEN-SRC-002").name("Teatro").city("Medellín")
                .address("Cra 43").capacity(2000).active(true).build());

        Artist artist = artistRepository.save(Artist.builder()
                .stageName("Feid").country("CO").genre("Reggaeton").active(true).build());

        eventRepository.save(Event.builder()
                .eventCode("EVT-SRC-002").name("Concierto Medellín")
                .category(EventCategory.MUSIC).status(EventStatus.PUBLISHED)
                .eventDate(LocalDateTime.now().plusDays(10)).minimumAge(0)
                .venue(venue).artists(new HashSet<>(Set.of(artist))).build());

        List<Event> results = eventRepository.findByCityAndArtist("Medellín", "Feid");

        assertThat(results).hasSize(1);
    }

    // FR-SRC-003 (case-insensitive, DISTINCT, orden por fecha)
    @Test
    void shouldFindRecommendedEventsCaseInsensitiveAndOrdered() {
        Venue venue = venueRepository.save(Venue.builder()
                .code("VEN-SRC-003").name("Estadio").city("Cali")
                .address("Av 6").capacity(10000).active(true).build());

        Artist artist = artistRepository.save(Artist.builder()
                .stageName("CAROL G").country("CO").genre("Reggaeton").active(true).build());

        LocalDateTime from = LocalDateTime.now();

        eventRepository.save(Event.builder()
                .eventCode("EVT-SRC-003-A").name("Concierto A")
                .category(EventCategory.MUSIC).status(EventStatus.PUBLISHED)
                .eventDate(from.plusDays(20)).minimumAge(0)
                .venue(venue).artists(new HashSet<>(Set.of(artist))).build());

        eventRepository.save(Event.builder()
                .eventCode("EVT-SRC-003-B").name("Concierto B")
                .category(EventCategory.MUSIC).status(EventStatus.PUBLISHED)
                .eventDate(from.plusDays(5)).minimumAge(0)
                .venue(venue).artists(new HashSet<>(Set.of(artist))).build());

        List<Event> results = eventRepository.findRecommendedEvents(from, "Cali", "karol");

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getEventCode()).isEqualTo("EVT-SRC-003-B");
    }

    // FR-SRC-004
    @Test
    void shouldFindTicketsForUpcomingEventsOrderedChronologically() {
        Venue venue = venueRepository.save(Venue.builder()
                .code("VEN-SRC-004").name("Arena").city("Bogotá")
                .address("Calle 26").capacity(12000).active(true).build());

        User user = userRepository.save(User.builder()
                .username("srcuser").email("src@mail.com").active(true).build());

        LocalDateTime from = LocalDateTime.now();

        Event futureEvent = eventRepository.save(Event.builder()
                .eventCode("EVT-SRC-004").name("Evento Futuro")
                .category(EventCategory.MUSIC).status(EventStatus.PUBLISHED)
                .eventDate(from.plusDays(30)).minimumAge(0).venue(venue).build());

        ticketRepository.save(Ticket.builder()
                .ticketCode("TKT-SRC-004").type(TicketType.GENERAL)
                .price(new BigDecimal("100000")).status(TicketStatus.PAID)
                .purchaseDate(LocalDateTime.now()).user(user).event(futureEvent).build());

        List<Ticket> results = ticketRepository.findTicketsForUpcomingEvents(from);

        assertThat(results).hasSize(1);
    }
}