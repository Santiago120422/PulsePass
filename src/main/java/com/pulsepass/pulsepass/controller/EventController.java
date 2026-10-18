package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.pulsepass.dto.response.EventResponse;
import com.pulsepass.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.pulsepass.service.EventService;
import jakarta.validation.Valid;
import com.pulsepass.pulsepass.domain.enums.EventStatus;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.security.SecurityUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid @RequestBody CreateEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(request));
    }

    @GetMapping("/{eventCode}")
    public ResponseEntity<EventResponse> findByCode(@PathVariable String eventCode, Authentication authentication) {
        EventResponse event = eventService.findByCode(eventCode);
        // FR-SECEVT-02: el publico solo ve eventos PUBLISHED. Para los demas responde 404 (no revela borradores).
        if (event.status() != EventStatus.PUBLISHED && !SecurityUtils.isStaffOrAdmin(authentication)) {
            throw ResourceNotFoundException.of("Event", eventCode);
        }
        return ResponseEntity.ok(event);
    }

    @GetMapping("/published")
    public ResponseEntity<List<EventSummaryResponse>> findPublishedEvents() {
        return ResponseEntity.ok(eventService.findPublishedEvents());
    }

    @PatchMapping("/{eventCode}/publish")
    public ResponseEntity<EventResponse> publish(@PathVariable String eventCode) {
        return ResponseEntity.ok(eventService.publish(eventCode));
    }

    @PostMapping("/{eventCode}/artists/{artistId}")
    public ResponseEntity<EventResponse> addArtist(@PathVariable String eventCode,
                                                   @PathVariable Long artistId) {
        return ResponseEntity.ok(eventService.addArtist(eventCode, artistId));
    }

    @GetMapping("/by-artist")
    public ResponseEntity<List<EventSummaryResponse>> findByArtist(@RequestParam String stageName,
                                                                   Authentication authentication) {
        List<EventSummaryResponse> events = eventService.findByArtist(stageName);
        // FR-SECEVT-06: sin STAFF/ADMIN solo se devuelven eventos publicados.
        if (!SecurityUtils.isStaffOrAdmin(authentication)) {
            events = events.stream().filter(e -> e.status() == EventStatus.PUBLISHED).toList();
        }
        return ResponseEntity.ok(events);
    }
}
