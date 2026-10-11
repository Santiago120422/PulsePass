package com.pulsepass.pulsepass.controller;

import com.pulsepass.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.pulsepass.domain.enums.TicketType;
import com.pulsepass.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.pulsepass.dto.response.TicketResponse;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.service.TicketService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
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

@WebMvcTest(TicketController.class)
class TicketControllerTest {

    private static final String VALID_BODY = """
            { "userEmail": "andrea@pulsepass.com", "eventCode": "CMF-2026", "type": "VIP" }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TicketService ticketService;

    private TicketResponse ticket(TicketStatus status) {
        return new TicketResponse(30L, "TKT-001", TicketType.VIP, new BigDecimal("300000.00"), status,
                LocalDateTime.of(2026, 10, 1, 10, 0), "andrea@pulsepass.com",
                "CMF-2026", "Caribbean Music Fest 2026");
    }

    // TEST-CTRL-TKT-001 / AC-CTRL009
    @Test
    void purchase_valid_returns201() throws Exception {
        when(ticketService.purchase(any(PurchaseTicketRequest.class))).thenReturn(ticket(TicketStatus.PAID));

        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketCode").value("TKT-001"))
                .andExpect(jsonPath("$.status").value("PAID"))
                .andExpect(jsonPath("$.price").value(300000.00));

        verify(ticketService).purchase(any(PurchaseTicketRequest.class));
    }

    // TEST-CTRL-TKT-002
    @Test
    void purchase_invalid_returns400AndDoesNotCallService() throws Exception {
        String body = """
                { "userEmail": "no-es-email" }
                """;

        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.details.userEmail").value("User email must be valid"))
                .andExpect(jsonPath("$.details.eventCode").value("Event code is required"))
                .andExpect(jsonPath("$.details.type").value("Ticket type is required"));

        verify(ticketService, never()).purchase(any());
    }

    @Test
    void purchase_invalidTicketType_returns400() throws Exception {
        String body = VALID_BODY.replace("VIP", "DIAMANTE");

        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());

        verify(ticketService, never()).purchase(any());
    }

    // TEST-CTRL-TKT-003
    @Test
    void purchase_unknownUser_returns404() throws Exception {
        when(ticketService.purchase(any(PurchaseTicketRequest.class)))
                .thenThrow(ResourceNotFoundException.of("User", "andrea@pulsepass.com"));

        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found: andrea@pulsepass.com"));
    }

    // TEST-CTRL-TKT-004 / AC-CTRL010
    @Test
    void purchase_businessRule_returns409() throws Exception {
        when(ticketService.purchase(any(PurchaseTicketRequest.class)))
                .thenThrow(new BusinessRuleException("User does not meet the minimum age."));

        mockMvc.perform(post("/api/tickets").contentType(MediaType.APPLICATION_JSON).content(VALID_BODY))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User does not meet the minimum age."));
    }

    // TEST-CTRL-TKT-005
    @Test
    void findByCode_existing_returns200() throws Exception {
        when(ticketService.findByCode("TKT-001")).thenReturn(ticket(TicketStatus.PAID));

        mockMvc.perform(get("/api/tickets/{ticketCode}", "TKT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketCode").value("TKT-001"))
                .andExpect(jsonPath("$.eventCode").value("CMF-2026"));

        verify(ticketService).findByCode("TKT-001");
    }

    @Test
    void findByCode_missing_returns404() throws Exception {
        when(ticketService.findByCode("NOPE")).thenThrow(ResourceNotFoundException.of("Ticket", "NOPE"));

        mockMvc.perform(get("/api/tickets/{ticketCode}", "NOPE"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Ticket not found: NOPE"));
    }

    // TEST-CTRL-TKT-006
    @Test
    void findByUser_returns200() throws Exception {
        when(ticketService.findByUserEmail("andrea@pulsepass.com"))
                .thenReturn(List.of(ticket(TicketStatus.PAID)));

        mockMvc.perform(get("/api/tickets/by-user").param("email", "andrea@pulsepass.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].userEmail").value("andrea@pulsepass.com"));

        verify(ticketService).findByUserEmail("andrea@pulsepass.com");
    }

    // TEST-CTRL-TKT-007
    @Test
    void findPaidByEvent_returns200() throws Exception {
        when(ticketService.findPaidTicketsByEvent("CMF-2026"))
                .thenReturn(List.of(ticket(TicketStatus.PAID)));

        mockMvc.perform(get("/api/events/{eventCode}/tickets/paid", "CMF-2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("PAID"));

        verify(ticketService).findPaidTicketsByEvent("CMF-2026");
    }

    // TEST-CTRL-TKT-008 / AC-CTRL011
    @Test
    void cancel_valid_returns200() throws Exception {
        when(ticketService.cancel("TKT-001")).thenReturn(ticket(TicketStatus.CANCELLED));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/cancel", "TKT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        verify(ticketService).cancel("TKT-001");
    }

    // TEST-CTRL-TKT-009 / AC-CTRL012
    @Test
    void cancel_usedTicket_returns409() throws Exception {
        when(ticketService.cancel("TKT-001"))
                .thenThrow(new BusinessRuleException("A USED ticket cannot be cancelled."));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/cancel", "TKT-001"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("A USED ticket cannot be cancelled."));
    }

    // TEST-CTRL-TKT-010 / AC-CTRL013
    @Test
    void use_valid_returns200() throws Exception {
        when(ticketService.markAsUsed("TKT-001")).thenReturn(ticket(TicketStatus.USED));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/use", "TKT-001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("USED"));

        verify(ticketService).markAsUsed("TKT-001");
    }

    // TEST-CTRL-TKT-011 / AC-CTRL014
    @Test
    void use_cancelledTicket_returns409() throws Exception {
        when(ticketService.markAsUsed("TKT-001"))
                .thenThrow(new BusinessRuleException("A CANCELLED ticket cannot be used."));

        mockMvc.perform(patch("/api/tickets/{ticketCode}/use", "TKT-001"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
