package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.AbstractIntegrationTest;
import com.pulsepass.pulsepass.domain.Artist;
import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.domain.enums.EventCategory;
import com.pulsepass.pulsepass.domain.enums.EventStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventArtistIT extends AbstractIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private VenueRepository venueRepository;

    // FR-ART-001 / FR-ART-002
    @Test
    void shouldPersistArtistAndRejectDuplicateStageName() {
        Artist artist = artistRepository.saveAndFlush(
                Artist.builder().stageName("Bad Bunny").country("PR").genre("Reggaeton").active(true).build());

        assertThat(artistRepository.findByStageName("Bad Bunny")).isPresent();

        Artist duplicate = Artist.builder()
                .stageName("Bad Bunny").country("PR").genre("Reggaeton").active(true).build();

        assertThrows(DataIntegrityViolationException.class,
                () -> artistRepository.saveAndFlush(duplicate));
    }

    // FR-ART-003 / QT-005 (Event N:M Artist, sin duplicar el par)
    @Test
    void shouldAssociateMultipleArtistsToEventWithoutDuplicatingPair() {
        Venue venue = venueRepository.save(Venue.builder()
                .code("VEN-ART-003").name("Estadio").city("Medellín")
                .address("Cra 70").capacity(20000).active(true).build());

        Artist a1 = artistRepository.save(Artist.builder()
                .stageName("Karol G").country("CO").genre("Reggaeton").active(true).build());
        Artist a2 = artistRepository.save(Artist.builder()
                .stageName("Shakira").country("CO").genre("Pop").active(true).build());

        Event event = Event.builder()
                .eventCode("EVT-ART-003")
                .name("Festival Colombia")
                .category(EventCategory.MUSIC)
                .status(EventStatus.PUBLISHED)
                .eventDate(LocalDateTime.now().plusDays(15))
                .minimumAge(0)
                .venue(venue)
                .build();

        Set<Artist> artists = new HashSet<>();
        artists.add(a1);
        artists.add(a2);
        event.setArtists(artists);

        Event saved = eventRepository.saveAndFlush(event);

        assertThat(saved.getArtists()).hasSize(2);
    }

    // FR-ART-004 (navegación inversa: eventos de un artista)
    // FR-SRC-001 (JPQL: eventos por stageName)
    @Test
    void shouldFindEventsByArtistStageName() {
        Venue venue = venueRepository.save(Venue.builder()
                .code("VEN-ART-004").name("Auditorio").city("Bogotá")
                .address("Calle 100").capacity(3000).active(true).build());

        Artist artist = artistRepository.save(Artist.builder()
                .stageName("J Balvin").country("CO").genre("Reggaeton").active(true).build());

        Event event1 = Event.builder()
                .eventCode("EVT-ART-004-A").name("Concierto 1")
                .category(EventCategory.MUSIC).status(EventStatus.PUBLISHED)
                .eventDate(LocalDateTime.now().plusDays(3)).minimumAge(0).venue(venue)
                .artists(new HashSet<>(Set.of(artist)))
                .build();

        Event event2 = Event.builder()
                .eventCode("EVT-ART-004-B").name("Concierto 2")
                .category(EventCategory.MUSIC).status(EventStatus.PUBLISHED)
                .eventDate(LocalDateTime.now().plusDays(8)).minimumAge(0).venue(venue)
                .artists(new HashSet<>(Set.of(artist)))
                .build();

        eventRepository.saveAndFlush(event1);
        eventRepository.saveAndFlush(event2);

        List<Event> events = eventRepository.findByArtistStageName("J Balvin");

        assertThat(events).hasSize(2);
    }
}