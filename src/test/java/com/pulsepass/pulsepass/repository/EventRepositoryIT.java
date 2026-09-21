package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.AbstractIntegrationTest;
import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.domain.enums.EventCategory;
import com.pulsepass.pulsepass.domain.enums.EventStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EventRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private VenueRepository venueRepository;

    private Venue persistVenue(String code) {
        return venueRepository.save(Venue.builder()
                .code(code).name("Coliseo").city("Cali")
                .address("Av 5").capacity(8000).active(true).build());
    }

    private Event.EventBuilder baseEvent(Venue venue, String eventCode, EventStatus status, LocalDateTime date) {
        return Event.builder()
                .eventCode(eventCode)
                .name("Festival de Prueba")
                .category(EventCategory.MUSIC)
                .status(status)
                .eventDate(date)
                .minimumAge(0)
                .venue(venue);
    }

    // FR-EVT-001 / BR-001
    @Test
    void shouldPersistEventAssociatedToExistingVenue() {
        Venue venue = persistVenue("VEN-EVT-001");
        Event event = baseEvent(venue, "EVT-001", EventStatus.DRAFT, LocalDateTime.now().plusDays(5)).build();

        Event saved = eventRepository.save(event);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVenue().getCode()).isEqualTo("VEN-EVT-001");
    }

    // FR-EVT-002
    @Test
    void shouldRejectDuplicateEventCode() {
        Venue venue = persistVenue("VEN-EVT-002");
        eventRepository.saveAndFlush(baseEvent(venue, "EVT-002", EventStatus.DRAFT, LocalDateTime.now()).build());

        Event duplicate = baseEvent(venue, "EVT-002", EventStatus.DRAFT, LocalDateTime.now()).build();
        assertThrows(DataIntegrityViolationException.class,
                () -> eventRepository.saveAndFlush(duplicate));
    }

    // FR-EVT-005
    @Test
    void shouldReturnOnlyPublishedEventsOrderedByDateAscending() {
        Venue venue = persistVenue("VEN-EVT-005");
        LocalDateTime now = LocalDateTime.now();

        eventRepository.save(baseEvent(venue, "EVT-005-A", EventStatus.PUBLISHED, now.plusDays(20)).build());
        eventRepository.save(baseEvent(venue, "EVT-005-B", EventStatus.PUBLISHED, now.plusDays(5)).build());
        eventRepository.save(baseEvent(venue, "EVT-005-C", EventStatus.DRAFT, now.plusDays(1)).build());

        List<Event> published = eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED);

        assertThat(published).hasSize(2);
        assertThat(published.get(0).getEventCode()).isEqualTo("EVT-005-B");
        assertThat(published.get(1).getEventCode()).isEqualTo("EVT-005-A");
    }

    // FR-EVT-006
    @Test
    void streamingUrlShouldBeOptional() {
        Venue venue = persistVenue("VEN-EVT-006");
        Event withoutStream = baseEvent(venue, "EVT-006", EventStatus.DRAFT, LocalDateTime.now()).build();

        Event saved = eventRepository.save(withoutStream);

        assertThat(saved.getStreamingUrl()).isNull();

        saved.setStreamingUrl("https://stream.pulsepass.com/evt-006");
        eventRepository.saveAndFlush(saved);

        assertThat(eventRepository.findById(saved.getId()).orElseThrow().getStreamingUrl())
                .isEqualTo("https://stream.pulsepass.com/evt-006");
    }
}