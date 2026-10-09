package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.domain.enums.EventCategory;
import com.pulsepass.pulsepass.domain.enums.EventStatus;
import com.pulsepass.pulsepass.dto.request.CreateEventRequest;
import com.pulsepass.pulsepass.dto.response.ArtistResponse;
import com.pulsepass.pulsepass.dto.response.EventResponse;
import com.pulsepass.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.service.EventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    private static final LocalDateTime DATE = LocalDateTime.of(2026, 12, 15, 20, 0);

    private static final String VALID_BODY = """
            {
              "eventCode": "CMF-2026",
              "name": "Caribbean Music Fest 2026",
              "description": "Festival de música del Caribe",
              "category": "MUSIC",
              "eventDate": "2026-12-15T20:00:00",
              "minimumAge": 18,
              "venueCode": "VEN-SMR-01"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    private EventResponse event(EventStatus status, List<ArtistResponse> artists) {
        return new EventResponse(10L, "CMF-2026", "Caribbean Music Fest 2026", "Festival",
                EventCategory.MUSIC, status, DATE, 18, null,
                "VEN-SMR-01", "Marina Convention Center", artists);
    }

    private EventSummaryResponse summary() {
        return new EventSummaryResponse(10L, "CMF-2026", "Caribbean Music Fest 2026",
                EventCategory.MUSIC, EventStatus.PUBLISHED, DATE,
                "VEN-SMR-01", "Marina Convention Center");
    }

    // TEST-CTRL-EVT-001 / AC-CTRL003
    @Test
    void create_valid_returns201() throws Exception {
        when(eventService.create(any(CreateEventRequest.class)))
                .thenReturn(event(EventStatus.DRAFT, List.of()));

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.venueCode").value("VEN-SMR-01"));

        verify(eventService).create(any(CreateEventRequest.class));
    }

    // TEST-CTRL-EVT-002 / AC-CTRL004
    @Test
    void create_invalid_returns400WithDetailsAndDoesNotCallService() throws Exception {
        String body = """
                { "name": "Sin código", "category": "MUSIC",
                  "eventDate": "2026-12-15T20:00:00", "minimumAge": 18 }
                """;

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.eventCode").value("Event code is required"))
                .andExpect(jsonPath("$.details.venueCode").value("Venue code is required"));

        verify(eventService, never()).create(any());
    }

    @Test
    void create_negativeMinimumAge_returns400() throws Exception {
        String body = VALID_BODY.replace("\"minimumAge\": 18", "\"minimumAge\": -1");

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.details.minimumAge").value("Minimum age must be at least 0"));

        verify(eventService, never()).create(any());
    }

    @Test
    void create_invalidCategory_returns400() throws Exception {
        String body = VALID_BODY.replace("MUSIC", "NO_EXISTE");

        mockMvc.perform(post("/api/events").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(eventService, never()).create(any());
    }

    // TEST-CTRL-EVT-003
    @Test
    void findByCode_existing_returns200() throws Exception {
        when(eventService.findByCode("CMF-2026")).thenReturn(event(EventStatus.DRAFT, List.of()));

        mockMvc.perform(get("/api/events/{eventCode}", "CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$.name").value("Caribbean Music Fest 2026"));

        verify(eventService).findByCode("CMF-2026");
    }

    // TEST-CTRL-EVT-004
    @Test
    void findByCode_missing_returns404() throws Exception {
        when(eventService.findByCode("NOPE")).thenThrow(ResourceNotFoundException.of("Event", "NOPE"));

        mockMvc.perform(get("/api/events/{eventCode}", "NOPE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Event not found: NOPE"));
    }

    // TEST-CTRL-EVT-005
    @Test
    void findPublishedEvents_returns200() throws Exception {
        when(eventService.findPublishedEvents()).thenReturn(List.of(summary()));

        mockMvc.perform(get("/api/events/published"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"))
                .andExpect(jsonPath("$[0].status").value("PUBLISHED"));

        verify(eventService).findPublishedEvents();
    }

    // TEST-CTRL-EVT-006 / AC-CTRL005
    @Test
    void publish_valid_returns200() throws Exception {
        when(eventService.publish("CMF-2026")).thenReturn(event(EventStatus.PUBLISHED, List.of()));

        mockMvc.perform(patch("/api/events/{eventCode}/publish", "CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        verify(eventService).publish("CMF-2026");
    }

    // TEST-CTRL-EVT-007 / AC-CTRL006
    @Test
    void publish_businessRuleViolation_returns409() throws Exception {
        when(eventService.publish("CMF-2026"))
                .thenThrow(new BusinessRuleException("Only DRAFT events can be published."));

        mockMvc.perform(patch("/api/events/{eventCode}/publish", "CMF-2026"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Only DRAFT events can be published."));
    }

    // TEST-CTRL-EVT-008
    @Test
    void addArtist_valid_returns200() throws Exception {
        ArtistResponse solar = new ArtistResponse(1L, "Solar Beat", "Colombia", "Electronic", true);
        when(eventService.addArtist("CMF-2026", 1L))
                .thenReturn(event(EventStatus.DRAFT, List.of(solar)));

        mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", "CMF-2026", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.artists.length()").value(1))
                .andExpect(jsonPath("$.artists[0].stageName").value("Solar Beat"));

        verify(eventService).addArtist("CMF-2026", 1L);
    }

    @Test
    void addArtist_duplicate_returns409() throws Exception {
        when(eventService.addArtist("CMF-2026", 1L))
                .thenThrow(new BusinessRuleException("Artist already associated with event."));

        mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", "CMF-2026", 1))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void addArtist_missingArtist_returns404() throws Exception {
        when(eventService.addArtist("CMF-2026", 99L))
                .thenThrow(ResourceNotFoundException.of("Artist", 99L));

        mockMvc.perform(post("/api/events/{eventCode}/artists/{artistId}", "CMF-2026", 99))
                .andExpect(status().isNotFound());
    }

    // TEST-CTRL-EVT-009
    @Test
    void findByArtist_returns200() throws Exception {
        when(eventService.findByArtist("Solar Beat")).thenReturn(List.of(summary()));

        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventCode").value("CMF-2026"));

        verify(eventService).findByArtist("Solar Beat");
    }
}
