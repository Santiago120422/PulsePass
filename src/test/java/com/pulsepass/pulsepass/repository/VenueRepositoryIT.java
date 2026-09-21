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

class VenueRepositoryIT  extends AbstractIntegrationTest {

    @Autowired
    private VenueRepository venueRepository;

    @Autowired
    private EventRepository eventRepository;

    private Venue newVenue(String code) {
        return Venue.builder()
                .code(code)
                .name("Movistar Arena")
                .city("Bogotá")
                .address("Calle 63 #47")
                .capacity(15000)
                .active(true)
                .build();
    }

    // FR-VEN-001
    @Test
    void shouldPersistAndRetrieveVenueByIdAndCode() {
        Venue saved = venueRepository.save(newVenue("VEN-001"));

        assertThat(venueRepository.findById(saved.getId())).isPresent();
        assertThat(venueRepository.findByCode("VEN-001")).isPresent();
    }

    // FR-VEN-002 / BR-009
    @Test
    void shouldRejectDuplicateVenueCode() {
        venueRepository.saveAndFlush(newVenue("VEN-002"));

        Venue duplicate = newVenue("VEN-002");
        assertThrows(DataIntegrityViolationException.class,
                () -> venueRepository.saveAndFlush(duplicate));
    }

    // FR-VEN-003
    @Test
    void shouldRejectCapacityLessOrEqualToZero() {
        Venue invalid = newVenue("VEN-003");
        invalid.setCapacity(0);

        assertThrows(DataIntegrityViolationException.class,
                () -> venueRepository.saveAndFlush(invalid));
    }

    // FR-VEN-004 / QT-003 (Venue 1:N Event)
    @Test
    void shouldRetrieveEventsByVenueCode() {
        Venue venue = venueRepository.save(newVenue("VEN-004"));

        Event event = Event.builder()
                .eventCode("EVT-VEN-004")
                .name("Rock al Parque")
                .category(EventCategory.MUSIC)
                .status(EventStatus.PUBLISHED)
                .eventDate(LocalDateTime.now().plusDays(10))
                .minimumAge(0)
                .venue(venue)
                .build();
        eventRepository.save(event);

        List<Event> events = eventRepository.findByVenue_Code("VEN-004");

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getEventCode()).isEqualTo("EVT-VEN-004");
    }
}