package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.Artist;
import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.Venue;
import com.pulsepass.pulsepass.domain.enums.EventCategory;
import com.pulsepass.pulsepass.domain.enums.EventStatus;
import com.pulsepass.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.pulsepass.dto.response.EventResponse;
import com.pulsepass.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.DuplicateResourceException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.EventMapper;
import com.pulsepass.pulsepass.repository.ArtistRepository;
import com.pulsepass.pulsepass.repository.EventRepository;
import com.pulsepass.pulsepass.repository.VenueRepository;
import com.pulsepass.pulsepass.service.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.pulsepass.pulsepass.service.impl.Validations.*;

@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final Set<EventStatus> CLOSED_STATUSES =
            Set.of(EventStatus.CANCELLED, EventStatus.FINISHED);

    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final ArtistRepository artistRepository;
    private final EventMapper eventMapper;
    private final Clock clock;

    public EventServiceImpl(EventRepository eventRepository,
                            VenueRepository venueRepository,
                            ArtistRepository artistRepository,
                            EventMapper eventMapper,
                            Clock clock) {
        this.eventRepository = eventRepository;
        this.venueRepository = venueRepository;
        this.artistRepository = artistRepository;
        this.eventMapper = eventMapper;
        this.clock = clock;
    }

    @Override
    @Transactional
    public EventResponse create(CreateEventRequest request) {
        requireRequest(request);
        String eventCode = requireText(request.eventCode(), "eventCode");
        String name = requireText(request.name(), "name");
        String venueCode = requireText(request.venueCode(), "venueCode");
        EventCategory category = requireValue(request.category(), "category");
        LocalDateTime eventDate = requireValue(request.eventDate(), "eventDate");

        // BR-EVENT-006: 0 (o null) = sin restricción de edad
        int minimumAge = request.minimumAge() == null ? 0 : request.minimumAge();
        if (minimumAge < 0) {
            throw new BusinessRuleException("Minimum age must be zero or greater.");
        }

        // BR-EVENT-001
        if (eventRepository.existsByEventCode(eventCode)) {
            throw new DuplicateResourceException("Event code already exists: " + eventCode);
        }

        // BR-EVENT-002
        Venue venue = venueRepository.findByCode(venueCode)
                .orElseThrow(() -> ResourceNotFoundException.of("Venue", venueCode));

        // BR-EVENT-003
        if (!isActive(venue)) {
            throw new BusinessRuleException("Venue is not active: " + venueCode);
        }

        // BR-EVENT-004
        if (!isFuture(eventDate)) {
            throw new BusinessRuleException("Event date must be in the future.");
        }

        // BR-EVENT-005: el estado inicial no lo controla el request
        Event event = Event.builder()
                .eventCode(eventCode)
                .name(name)
                .description(request.description())
                .category(category)
                .status(EventStatus.DRAFT)
                .eventDate(eventDate)
                .minimumAge(minimumAge)
                .venue(venue)
                .build();

        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    public EventResponse findByCode(String eventCode) {
        return eventMapper.toResponse(getEvent(eventCode));
    }

    @Override
    public List<EventSummaryResponse> findPublishedEvents() {
        return eventRepository.findByStatusOrderByEventDateAsc(EventStatus.PUBLISHED)
                .stream()
                .map(eventMapper::toSummary)
                .toList();
    }

    @Override
    @Transactional
    public EventResponse publish(String eventCode) {
        Event event = getEvent(eventCode);

        // BR-EVENT-007
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new BusinessRuleException(
                    "Only DRAFT events can be published. Event " + eventCode
                            + " is " + event.getStatus() + ".");
        }
        // BR-EVENT-008
        if (!isFuture(event.getEventDate())) {
            throw new BusinessRuleException("Cannot publish an event whose date has passed: " + eventCode);
        }
        // BR-EVENT-009
        if (!isActive(event.getVenue())) {
            throw new BusinessRuleException(
                    "Cannot publish an event in an inactive venue: " + event.getVenue().getCode());
        }

        event.setStatus(EventStatus.PUBLISHED);
        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventResponse addArtist(String eventCode, Long artistId) {
        Event event = getEvent(eventCode);
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> ResourceNotFoundException.of("Artist", artistId));

        // BR-EVENT-011
        if (CLOSED_STATUSES.contains(event.getStatus())) {
            throw new BusinessRuleException(
                    "Cannot add artists to a " + event.getStatus() + " event: " + eventCode);
        }
        // BR-EVENT-010
        boolean alreadyAssociated = event.getArtists().stream()
                .anyMatch(a -> Objects.equals(a.getId(), artist.getId()));
        if (alreadyAssociated) {
            throw new DuplicateResourceException(
                    "Artist " + artist.getStageName() + " is already associated with event " + eventCode);
        }

        event.getArtists().add(artist);
        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    public List<EventSummaryResponse> findByArtist(String stageName) {
        return eventRepository.findByArtistStageName(stageName)
                .stream()
                .map(eventMapper::toSummary)
                .toList();
    }

    // ---------- helpers

    private Event getEvent(String eventCode) {
        return eventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> ResourceNotFoundException.of("Event", eventCode));
    }

    private boolean isFuture(LocalDateTime date) {
        return date.isAfter(LocalDateTime.now(clock));
    }

    private static boolean isActive(Venue venue) {
        return Boolean.TRUE.equals(venue.getActive());
    }
}
