package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.*;
import com.pulsepass.pulsepass.domain.enums.*;
import com.pulsepass.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.pulsepass.dto.response.EventResponse;
import com.pulsepass.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.pulsepass.exception.*;
import com.pulsepass.pulsepass.mapper.EventMapper;
import com.pulsepass.pulsepass.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.pulsepass.pulsepass.service.impl.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    private static final String CODE = "CMF-2026";
    private static final LocalDateTime FUTURE = NOW.plusDays(60);

    @Mock private EventRepository eventRepository;
    @Mock private VenueRepository venueRepository;
    @Mock private ArtistRepository artistRepository;
    @Mock private EventMapper eventMapper;

    private EventServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EventServiceImpl(eventRepository, venueRepository, artistRepository, eventMapper, CLOCK);
    }

    private static EventResponse response(EventStatus status) {
        return new EventResponse(10L, CODE, "Caribbean Music Fest 2026", null, EventCategory.MUSIC,
                status, FUTURE, 18, null, "VEN-SMR-01", "Marina Convention Center", List.of());
    }

    private static CreateEventRequest request(LocalDateTime date, Integer minAge) {
        return new CreateEventRequest(CODE, "Caribbean Music Fest 2026", "desc",
                EventCategory.MUSIC, date, minAge, "VEN-SMR-01");
    }

    // ---------------- findByCode

    @Test // TEST-EVENT-001
    void findByCode_existingEvent_returnsDto() {
        Event event = event(CODE, EventStatus.DRAFT, FUTURE, 18, venue("VEN-SMR-01", 3, true));
        when(eventRepository.findByEventCode(CODE)).thenReturn(Optional.of(event));
        when(eventMapper.toResponse(event)).thenReturn(response(EventStatus.DRAFT));

        EventResponse result = service.findByCode(CODE);

        assertThat(result.eventCode()).isEqualTo(CODE);
        verify(eventMapper).toResponse(event);
    }

    @Test // TEST-EVENT-002
    void findByCode_missingEvent_throwsResourceNotFound() {
        when(eventRepository.findByEventCode(CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode(CODE))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Event not found: CMF-2026");
        verifyNoInteractions(eventMapper);
    }

    @Test
    void findPublishedEvents_mapsEveryEventToSummary() {
        Event event = event(CODE, EventStatus.PUBLISHED, FUTURE, 18, venue("VEN-SMR-01", 3, true));
        EventSummaryResponse summary = new EventSummaryResponse(10L, CODE, "n", EventCategory.MUSIC,
                EventStatus.PUBLISHED, FUTURE, "VEN-SMR-01", "Marina Convention Center");
        when(eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED)).thenReturn(List.of(event));
        when(eventMapper.toSummary(event)).thenReturn(summary);

        assertThat(service.findPublishedEvents()).containsExactly(summary);
    }

    // ---------------- create

    @Test // TEST-EVENT-003 + BR-EVENT-005
    void create_validRequest_savesEventAsDraft() {
        Venue venue = venue("VEN-SMR-01", 3, true);
        when(eventRepository.existsByEventCode(CODE)).thenReturn(false);
        when(venueRepository.findByCode("VEN-SMR-01")).thenReturn(Optional.of(venue));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventMapper.toResponse(any(Event.class))).thenReturn(response(EventStatus.DRAFT));

        EventResponse result = service.create(request(FUTURE, 18));

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        Event saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(EventStatus.DRAFT);
        assertThat(saved.getVenue()).isSameAs(venue);
        assertThat(saved.getEventCode()).isEqualTo(CODE);
        assertThat(saved.getMinimumAge()).isEqualTo(18);
        assertThat(result.status()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    void create_nullMinimumAge_meansNoRestriction() {
        when(eventRepository.existsByEventCode(CODE)).thenReturn(false);
        when(venueRepository.findByCode("VEN-SMR-01")).thenReturn(Optional.of(venue("VEN-SMR-01", 3, true)));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventMapper.toResponse(any(Event.class))).thenReturn(response(EventStatus.DRAFT));

        service.create(request(FUTURE, null));

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository).save(captor.capture());
        assertThat(captor.getValue().getMinimumAge()).isZero();
    }

    @Test // BR-EVENT-001
    void create_duplicateCode_throwsDuplicateResource() {
        when(eventRepository.existsByEventCode(CODE)).thenReturn(true);

        assertThatThrownBy(() -> service.create(request(FUTURE, 18)))
                .isInstanceOf(DuplicateResourceException.class);
        verify(eventRepository, never()).save(any());
        verifyNoInteractions(venueRepository);
    }

    @Test // TEST-EVENT-004
    void create_missingVenue_throwsNotFoundAndNeverSaves() {
        when(eventRepository.existsByEventCode(CODE)).thenReturn(false);
        when(venueRepository.findByCode("VEN-SMR-01")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request(FUTURE, 18)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("VEN-SMR-01");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test // TEST-EVENT-005
    void create_inactiveVenue_throwsBusinessRule() {
        when(eventRepository.existsByEventCode(CODE)).thenReturn(false);
        when(venueRepository.findByCode("VEN-SMR-01")).thenReturn(Optional.of(venue("VEN-SMR-01", 3, false)));

        assertThatThrownBy(() -> service.create(request(FUTURE, 18)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("not active");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test // TEST-EVENT-006
    void create_pastDate_throwsBusinessRule() {
        when(eventRepository.existsByEventCode(CODE)).thenReturn(false);
        when(venueRepository.findByCode("VEN-SMR-01")).thenReturn(Optional.of(venue("VEN-SMR-01", 3, true)));

        assertThatThrownBy(() -> service.create(request(NOW.minusDays(1), 18)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("future");
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test // BR-EVENT-006
    void create_negativeMinimumAge_throwsBusinessRule() {
        assertThatThrownBy(() -> service.create(request(FUTURE, -1)))
                .isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    // ---------------- publish

    @Test // TEST-EVENT-007
    void publish_validDraft_becomesPublished() {
        Event event = event(CODE, EventStatus.DRAFT, FUTURE, 18, venue("VEN-SMR-01", 3, true));
        when(eventRepository.findByEventCode(CODE)).thenReturn(Optional.of(event));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response(EventStatus.PUBLISHED));

        EventResponse result = service.publish(CODE);

        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(result.status()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository).save(event);
    }

    @Test // TEST-EVENT-008
    void publish_cancelledEvent_throwsBusinessRuleAndDoesNotPersist() {
        Event event = event(CODE, EventStatus.CANCELLED, FUTURE, 18, venue("VEN-SMR-01", 3, true));
        when(eventRepository.findByEventCode(CODE)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.publish(CODE)).isInstanceOf(BusinessRuleException.class);

        assertThat(event.getStatus()).isEqualTo(EventStatus.CANCELLED);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void publish_alreadyPublished_throwsBusinessRule() {
        Event event = event(CODE, EventStatus.PUBLISHED, FUTURE, 18, venue("VEN-SMR-01", 3, true));
        when(eventRepository.findByEventCode(CODE)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.publish(CODE)).isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test // BR-EVENT-008
    void publish_pastDate_throwsBusinessRule() {
        Event event = event(CODE, EventStatus.DRAFT, NOW.minusHours(1), 18, venue("VEN-SMR-01", 3, true));
        when(eventRepository.findByEventCode(CODE)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.publish(CODE)).isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test // BR-EVENT-009
    void publish_inactiveVenue_throwsBusinessRule() {
        Event event = event(CODE, EventStatus.DRAFT, FUTURE, 18, venue("VEN-SMR-01", 3, false));
        when(eventRepository.findByEventCode(CODE)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.publish(CODE)).isInstanceOf(BusinessRuleException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void publish_missingEvent_throwsNotFound() {
        when(eventRepository.findByEventCode(CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.publish(CODE)).isInstanceOf(ResourceNotFoundException.class);
    }

    // ---------------- addArtist

    @Test
    void addArtist_validArtist_isAssociatedAndSaved() {
        Event event = event(CODE, EventStatus.DRAFT, FUTURE, 18, venue("VEN-SMR-01", 3, true));
        Artist artist = artist(1L, "Solar Beat");
        when(eventRepository.findByEventCode(CODE)).thenReturn(Optional.of(event));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(eventRepository.save(event)).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response(EventStatus.DRAFT));

        service.addArtist(CODE, 1L);

        assertThat(event.getArtists()).containsExactly(artist);
        verify(eventRepository).save(event);
    }

    @Test // BR-EVENT-010
    void addArtist_alreadyAssociated_throwsDuplicateResource() {
        Event event = event(CODE, EventStatus.DRAFT, FUTURE, 18, venue("VEN-SMR-01", 3, true));
        event.getArtists().add(artist(1L, "Solar Beat"));
        when(eventRepository.findByEventCode(CODE)).thenReturn(Optional.of(event));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist(1L, "Solar Beat")));

        assertThatThrownBy(() -> service.addArtist(CODE, 1L))
                .isInstanceOf(DuplicateResourceException.class);
        assertThat(event.getArtists()).hasSize(1);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test // BR-EVENT-011
    void addArtist_toCancelledOrFinishedEvent_throwsBusinessRule() {
        for (EventStatus status : List.of(EventStatus.CANCELLED, EventStatus.FINISHED)) {
            Event event = event(CODE, status, FUTURE, 18, venue("VEN-SMR-01", 3, true));
            when(eventRepository.findByEventCode(CODE)).thenReturn(Optional.of(event));
            when(artistRepository.findById(1L)).thenReturn(Optional.of(artist(1L, "Solar Beat")));

            assertThatThrownBy(() -> service.addArtist(CODE, 1L))
                    .isInstanceOf(BusinessRuleException.class);
            assertThat(event.getArtists()).isEmpty();
        }
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void addArtist_missingArtist_throwsNotFound() {
        Event event = event(CODE, EventStatus.DRAFT, FUTURE, 18, venue("VEN-SMR-01", 3, true));
        when(eventRepository.findByEventCode(CODE)).thenReturn(Optional.of(event));
        when(artistRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addArtist(CODE, 99L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void addArtist_missingEvent_throwsNotFoundWithoutLookingForArtist() {
        when(eventRepository.findByEventCode(CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addArtist(CODE, 1L)).isInstanceOf(ResourceNotFoundException.class);
        verifyNoInteractions(artistRepository);
    }

    @Test
    void findByArtist_mapsEventsToSummaries() {
        Event event = event(CODE, EventStatus.PUBLISHED, FUTURE, 18, venue("VEN-SMR-01", 3, true));
        EventSummaryResponse summary = new EventSummaryResponse(10L, CODE, "n", EventCategory.MUSIC,
                EventStatus.PUBLISHED, FUTURE, "VEN-SMR-01", "Marina Convention Center");
        when(eventRepository.findByArtistStageName("Solar Beat")).thenReturn(List.of(event));
        when(eventMapper.toSummary(event)).thenReturn(summary);

        assertThat(service.findByArtist("Solar Beat")).containsExactly(summary);
    }
}
