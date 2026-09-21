package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.AbstractIntegrationTest;
import com.pulsepass.pulsepass.domain.*;
import com.pulsepass.pulsepass.domain.enums.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TicketRepositoryIT extends AbstractIntegrationTest {

    @Autowired private TicketRepository ticketRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private VenueRepository venueRepository;

    private Venue venue(String code) {
        return venueRepository.save(Venue.builder()
                .code(code).name("Venue").city("Bogotá")
                .address("Cra 1").capacity(5000).active(true).build());
    }

    private Event event(String code, Venue v, LocalDateTime date) {
        return eventRepository.save(Event.builder()
                .eventCode(code).name("Evento").category(EventCategory.MUSIC)
                .status(EventStatus.PUBLISHED).eventDate(date).minimumAge(0).venue(v).build());
    }

    private User user(String username, String email) {
        return userRepository.save(User.builder().username(username).email(email).active(true).build());
    }

    private Ticket.TicketBuilder baseTicket(String code, User u, Event e, TicketStatus status) {
        return Ticket.builder()
                .ticketCode(code)
                .type(TicketType.GENERAL)
                .price(new BigDecimal("150000.00"))
                .status(status)
                .purchaseDate(LocalDateTime.now())
                .user(u)
                .event(e);
    }

    // FR-TKT-001 / BR-005 / BR-006
    @Test
    void shouldPersistTicketLinkedToUserAndEvent() {
        Venue v = venue("VEN-TKT-001");
        User u = user("ticketuser1", "t1@mail.com");
        Event e = event("EVT-TKT-001", v, LocalDateTime.now().plusDays(5));

        Ticket saved = ticketRepository.save(baseTicket("TKT-001", u, e, TicketStatus.RESERVED).build());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getUsername()).isEqualTo("ticketuser1");
        assertThat(saved.getEvent().getEventCode()).isEqualTo("EVT-TKT-001");
    }

    // FR-TKT-002
    @Test
    void shouldRejectDuplicateTicketCode() {
        Venue v = venue("VEN-TKT-002");
        User u = user("ticketuser2", "t2@mail.com");
        Event e = event("EVT-TKT-002", v, LocalDateTime.now().plusDays(5));

        ticketRepository.saveAndFlush(baseTicket("TKT-002", u, e, TicketStatus.PAID).build());

        Ticket duplicate = baseTicket("TKT-002", u, e, TicketStatus.PAID).build();
        assertThrows(DataIntegrityViolationException.class,
                () -> ticketRepository.saveAndFlush(duplicate));
    }

    // FR-TKT-003 / BR-007
    @Test
    void shouldRejectNegativePrice() {
        Venue v = venue("VEN-TKT-003");
        User u = user("ticketuser3", "t3@mail.com");
        Event e = event("EVT-TKT-003", v, LocalDateTime.now().plusDays(5));

        Ticket invalid = baseTicket("TKT-003", u, e, TicketStatus.RESERVED)
                .price(new BigDecimal("-1.00")).build();

        assertThrows(DataIntegrityViolationException.class,
                () -> ticketRepository.saveAndFlush(invalid));
    }

    // FR-TKT-006
    @Test
    void shouldFindTicketsByUserEmailAndStatus() {
        Venue v = venue("VEN-TKT-006");
        User u = user("ticketuser6", "t6@mail.com");
        Event e = event("EVT-TKT-006", v, LocalDateTime.now().plusDays(5));

        ticketRepository.save(baseTicket("TKT-006-A", u, e, TicketStatus.PAID).build());
        ticketRepository.save(baseTicket("TKT-006-B", u, e, TicketStatus.RESERVED).build());

        List<Ticket> paidTickets = ticketRepository.findByUser_EmailAndStatus("t6@mail.com", TicketStatus.PAID);

        assertThat(paidTickets).hasSize(1);
        assertThat(paidTickets.get(0).getTicketCode()).isEqualTo("TKT-006-A");
    }

    // FR-TKT-007
    @Test
    void shouldFindPaidTicketsByEventCode() {
        Venue v = venue("VEN-TKT-007");
        User u1 = user("ticketuser7a", "t7a@mail.com");
        User u2 = user("ticketuser7b", "t7b@mail.com");
        Event e = event("EVT-TKT-007", v, LocalDateTime.now().plusDays(5));

        ticketRepository.save(baseTicket("TKT-007-A", u1, e, TicketStatus.PAID).build());
        ticketRepository.save(baseTicket("TKT-007-B", u2, e, TicketStatus.CANCELLED).build());

        List<Ticket> paid = ticketRepository.findByEvent_EventCodeAndStatus("EVT-TKT-007", TicketStatus.PAID);

        assertThat(paid).hasSize(1);
    }

    // FR-TKT-008
    @Test
    void shouldCountPaidTicketsForEvent() {
        Venue v = venue("VEN-TKT-008");
        User u1 = user("ticketuser8a", "t8a@mail.com");
        User u2 = user("ticketuser8b", "t8b@mail.com");
        Event e = event("EVT-TKT-008", v, LocalDateTime.now().plusDays(5));

        ticketRepository.save(baseTicket("TKT-008-A", u1, e, TicketStatus.PAID).build());
        ticketRepository.save(baseTicket("TKT-008-B", u2, e, TicketStatus.PAID).build());
        ticketRepository.save(baseTicket("TKT-008-C", u2, e, TicketStatus.CANCELLED).build());

        long count = ticketRepository.countByEvent_EventCodeAndStatus("EVT-TKT-008", TicketStatus.PAID);

        assertThat(count).isEqualTo(2);
    }
}