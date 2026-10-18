package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.domain.enums.EventCategory;
import com.pulsepass.pulsepass.domain.enums.EventStatus;
import com.pulsepass.pulsepass.dto.response.EventResponse;
import com.pulsepass.pulsepass.dto.response.EventSummaryResponse;
import com.pulsepass.pulsepass.service.*;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Matriz de seguridad por URL/rol (T-SEC-001..004, 007, 008, 012..014, 017). Los Services estan mockeados. */
@WebMvcTest(controllers = {EventController.class, TicketController.class, UserController.class,
        VenueController.class, ArtistController.class, AuthController.class})
class SecurityMatrixTest extends ControllerSecurityTestBase {

    @MockitoBean EventService eventService;
    @MockitoBean TicketService ticketService;
    @MockitoBean UserService userService;
    @MockitoBean AuthService authService;
    @MockitoBean VenueService venueService;
    @MockitoBean ArtistService artistService;

    private EventResponse event(EventStatus status) {
        return new EventResponse(1L, "CMF-2026", "Festival", "desc", EventCategory.MUSIC, status,
                LocalDateTime.of(2027, 1, 1, 20, 0), 18, null, "VEN-1", "Venue", List.of());
    }

    private EventSummaryResponse summary(String code, EventStatus status) {
        return new EventSummaryResponse(1L, code, "Festival", EventCategory.MUSIC, status,
                LocalDateTime.of(2027, 1, 1, 20, 0), "VEN-1", "Venue");
    }

    // ---------- publicas ----------
    @Test
    void anonymous_canReadPublicEndpoints() throws Exception {
        when(eventService.findPublishedEvents()).thenReturn(List.of());
        mockMvc.perform(get("/api/events/published")).andExpect(status().isOk());
        mockMvc.perform(get("/api/venues/active")).andExpect(status().isOk());
        mockMvc.perform(get("/api/artists/active")).andExpect(status().isOk());
    }

    @Test
    void anonymous_cannotReachProtectedRoutes_andGetsErrorResponse401() throws Exception {
        mockMvc.perform(post("/api/events").contentType("application/json").content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.details").isMap());
        mockMvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/tickets/TKT-1")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users/by-email").param("email", "a@b.com")).andExpect(status().isUnauthorized());
        verifyNoInteractions(eventService, ticketService, userService);
    }

    // ---------- roles ----------
    @Test
    @WithMockUser(roles = "USER")
    void user_cannotCreatePublishOrAssociateArtist_andServiceIsNotInvoked() throws Exception {
        mockMvc.perform(post("/api/events").contentType("application/json").content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
        mockMvc.perform(patch("/api/events/CMF-2026/publish")).andExpect(status().isForbidden());
        mockMvc.perform(post("/api/events/CMF-2026/artists/1")).andExpect(status().isForbidden());
        verifyNoInteractions(eventService);
    }

    @Test
    @WithMockUser(roles = "USER")
    void user_cannotUseTicketsNorListPaidTickets() throws Exception {
        mockMvc.perform(patch("/api/tickets/TKT-1/use")).andExpect(status().isForbidden());
        mockMvc.perform(get("/api/events/CMF-2026/tickets/paid")).andExpect(status().isForbidden());
        verifyNoInteractions(ticketService);
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staff_canUseTickets_butCannotPublish() throws Exception {
        when(ticketService.markAsUsed(anyString())).thenReturn(null);
        mockMvc.perform(patch("/api/tickets/TKT-1/use")).andExpect(status().isOk());
        mockMvc.perform(patch("/api/events/CMF-2026/publish")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void admin_canPublishAndAssociateArtist() throws Exception {
        when(eventService.publish("CMF-2026")).thenReturn(event(EventStatus.PUBLISHED));
        when(eventService.addArtist("CMF-2026", 1L)).thenReturn(event(EventStatus.PUBLISHED));
        mockMvc.perform(patch("/api/events/CMF-2026/publish")).andExpect(status().isOk());
        mockMvc.perform(post("/api/events/CMF-2026/artists/1")).andExpect(status().isOk());
    }

    // ---------- JWT ----------
    @Test
    void invalidBearer_isRejectedWith401_evenOnPublicRoutes_andNoBusinessIsInvoked() throws Exception {
        when(jwtService.parse("malo")).thenThrow(new JwtException("invalid"));
        mockMvc.perform(get("/api/tickets/TKT-1").header("Authorization", "Bearer malo"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        mockMvc.perform(get("/api/events/published").header("Authorization", "Bearer malo"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(ticketService, eventService);
    }

    @Test
    void nonBearerAuthorizationHeader_isRejected() throws Exception {
        mockMvc.perform(get("/api/events/published").header("Authorization", "Basic abc"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- borradores ----------
    @Test
    void anonymous_cannotSeeDraftEvent_butSeesPublished() throws Exception {
        when(eventService.findByCode("DRAFT-1")).thenReturn(event(EventStatus.DRAFT));
        when(eventService.findByCode("CMF-2026")).thenReturn(event(EventStatus.PUBLISHED));
        mockMvc.perform(get("/api/events/DRAFT-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
        mockMvc.perform(get("/api/events/CMF-2026")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STAFF")
    void staff_canSeeDraftEvent() throws Exception {
        when(eventService.findByCode("DRAFT-1")).thenReturn(event(EventStatus.DRAFT));
        mockMvc.perform(get("/api/events/DRAFT-1")).andExpect(status().isOk());
    }

    @Test
    void byArtist_filtersNonPublishedForAnonymous() throws Exception {
        when(eventService.findByArtist("Solar Beat")).thenReturn(
                List.of(summary("PUB", EventStatus.PUBLISHED), summary("DRAFT", EventStatus.DRAFT)));
        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].eventCode").value("PUB"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void byArtist_returnsEverythingForAdmin() throws Exception {
        when(eventService.findByArtist("Solar Beat")).thenReturn(
                List.of(summary("PUB", EventStatus.PUBLISHED), summary("DRAFT", EventStatus.DRAFT)));
        mockMvc.perform(get("/api/events/by-artist").param("stageName", "Solar Beat"))
                .andExpect(jsonPath("$.length()").value(2));
    }
}
