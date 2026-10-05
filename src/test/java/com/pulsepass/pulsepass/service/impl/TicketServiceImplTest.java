package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.*;
import com.pulsepass.pulsepass.domain.enums.*;
import com.pulsepass.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.pulsepass.dto.response.TicketResponse;
import com.pulsepass.pulsepass.exception.*;
import com.pulsepass.pulsepass.mapper.TicketMapper;
import com.pulsepass.pulsepass.repository.*;
import com.pulsepass.pulsepass.service.pricing.PricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.pulsepass.pulsepass.service.impl.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    private static final String EVENT_CODE = "CMF-2026";
    private static final String EMAIL = "andrea@email.com";
    private static final LocalDateTime EVENT_DATE = NOW.plusDays(60);
    private static final LocalDate ADULT_BIRTH = LocalDate.of(2000, 1, 1);

    @Mock private TicketRepository ticketRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventRepository eventRepository;
    @Mock private TicketMapper ticketMapper;
    @Mock private PricingStrategy pricingStrategy;

    private TicketServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TicketServiceImpl(ticketRepository, userRepository, eventRepository,
                ticketMapper, pricingStrategy, CLOCK);
    }

    private static PurchaseTicketRequest request() {
        return new PurchaseTicketRequest(EMAIL, EVENT_CODE, TicketType.VIP);
    }

    private static TicketResponse response(TicketStatus status) {
        return new TicketResponse(30L, "TKT-1", TicketType.VIP, new BigDecimal("300000.00"), status,
                NOW, EMAIL, EVENT_CODE, "Caribbean Music Fest 2026");
    }

    private Event publishedEvent(int capacity) {
        return event(EVENT_CODE, EventStatus.PUBLISHED, EVENT_DATE, 18, venue("VEN-SMR-01", capacity, true));
    }

    private void stubUser(User user) {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.of(user));
    }

    private void stubEvent(Event event) {
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.of(event));
    }

    private void stubOccupiedSeats(long occupied) {
        when(ticketRepository.countByEvent_EventCodeAndStatusIn(eq(EVENT_CODE), anyCollection()))
                .thenReturn(occupied);
    }

    // ================= purchase

    @Test // TEST-TICKET-001
    void purchase_validRequest_createsPaidTicket() {
        stubUser(user(EMAIL, true, ADULT_BIRTH));
        Event event = publishedEvent(3);
        stubEvent(event);
        stubOccupiedSeats(0);
        when(pricingStrategy.priceFor(TicketType.VIP)).thenReturn(new BigDecimal("300000.00"));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(response(TicketStatus.PAID));

        TicketResponse result = service.purchase(request());

        ArgumentCaptor<Ticket> captor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketRepository).save(captor.capture());
        Ticket saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TicketStatus.PAID);
        assertThat(saved.getType()).isEqualTo(TicketType.VIP);
        assertThat(saved.getPrice()).isEqualByComparingTo("300000.00");
        assertThat(saved.getPurchaseDate()).isEqualTo(NOW);
        assertThat(saved.getTicketCode()).startsWith("TKT-");
        assertThat(saved.getEvent()).isSameAs(event);
        assertThat(result.status()).isEqualTo(TicketStatus.PAID);
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test // TEST-TICKET-002
    void purchase_missingUser_throwsNotFound() {
        when(userRepository.findByEmailIgnoreCase(EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(ResourceNotFoundException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
        verifyNoInteractions(eventRepository);
    }

    @Test // TEST-TICKET-003
    void purchase_inactiveUser_throwsBusinessRule() {
        stubUser(user(EMAIL, false, ADULT_BIRTH));

        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test // BR-TICKET-003
    void purchase_missingEvent_throwsNotFound() {
        stubUser(user(EMAIL, true, ADULT_BIRTH));
        when(eventRepository.findByEventCode(EVENT_CODE)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(ResourceNotFoundException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test // TEST-TICKET-004
    void purchase_draftEvent_throwsBusinessRule() {
        stubUser(user(EMAIL, true, ADULT_BIRTH));
        stubEvent(event(EVENT_CODE, EventStatus.DRAFT, EVENT_DATE, 18, venue("V", 3, true)));

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("DRAFT");
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test // TEST-TICKET-005
    void purchase_cancelledEvent_throwsBusinessRule() {
        stubUser(user(EMAIL, true, ADULT_BIRTH));
        stubEvent(event(EVENT_CODE, EventStatus.CANCELLED, EVENT_DATE, 18, venue("V", 3, true)));

        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test // BR-TICKET-004
    void purchase_soldOutOrFinishedEvent_throwsBusinessRule() {
        stubUser(user(EMAIL, true, ADULT_BIRTH));
        for (EventStatus status : List.of(EventStatus.SOLD_OUT, EventStatus.FINISHED)) {
            stubEvent(event(EVENT_CODE, status, EVENT_DATE, 18, venue("V", 3, true)));

            assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        }
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test // BR-TICKET-005
    void purchase_eventAlreadyHappened_throwsBusinessRule() {
        stubUser(user(EMAIL, true, ADULT_BIRTH));
        stubEvent(event(EVENT_CODE, EventStatus.PUBLISHED, NOW.minusHours(1), 18, venue("V", 3, true)));

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("already taken place");
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test // TEST-TICKET-006
    void purchase_underageUser_throwsBusinessRule() {
        stubUser(user(EMAIL, true, LocalDate.of(2009, 6, 1))); // 17 años en la fecha del evento
        stubEvent(publishedEvent(3));

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(BusinessRuleException.class).hasMessage("User does not meet minimum age.");
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test // BR-TICKET-006: la edad se evalúa en la fecha del evento, no hoy
    void purchase_turnsEighteenBeforeEventDate_isAllowed() {
        // Hoy (2026-10-02) tiene 17; el evento es el 2026-12-01 y cumple 18 el 2026-11-01.
        stubUser(user(EMAIL, true, LocalDate.of(2008, 11, 1)));
        stubEvent(publishedEvent(3));
        stubOccupiedSeats(0);
        when(pricingStrategy.priceFor(TicketType.VIP)).thenReturn(new BigDecimal("300000.00"));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(response(TicketStatus.PAID));

        assertThat(service.purchase(request()).status()).isEqualTo(TicketStatus.PAID);
    }

    @Test
    void purchase_userWithoutBirthDateOnRestrictedEvent_throwsBusinessRule() {
        stubUser(user(EMAIL, true, null));
        stubEvent(publishedEvent(3));

        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void purchase_eventWithoutAgeRestriction_ignoresBirthDate() {
        stubUser(user(EMAIL, true, null));
        stubEvent(event(EVENT_CODE, EventStatus.PUBLISHED, EVENT_DATE, 0, venue("V", 3, true)));
        stubOccupiedSeats(0);
        when(pricingStrategy.priceFor(TicketType.VIP)).thenReturn(new BigDecimal("300000.00"));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(response(TicketStatus.PAID));

        assertThat(service.purchase(request())).isNotNull();
    }

    @Test // TEST-TICKET-007
    void purchase_noCapacityLeft_throwsBusinessRule() {
        stubUser(user(EMAIL, true, ADULT_BIRTH));
        stubEvent(publishedEvent(3));
        stubOccupiedSeats(3);

        assertThatThrownBy(() -> service.purchase(request()))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("capacity");
        verify(ticketRepository, never()).save(any(Ticket.class));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test // TEST-TICKET-008
    void purchase_lastAvailableTicket_savesTicketAndMarksEventSoldOut() {
        stubUser(user(EMAIL, true, ADULT_BIRTH));
        Event event = publishedEvent(3);
        stubEvent(event);
        stubOccupiedSeats(2);
        when(pricingStrategy.priceFor(TicketType.VIP)).thenReturn(new BigDecimal("300000.00"));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> inv.getArgument(0));
        when(eventRepository.save(event)).thenReturn(event);
        when(ticketMapper.toResponse(any(Ticket.class))).thenReturn(response(TicketStatus.PAID));

        service.purchase(request());

        verify(ticketRepository).save(any(Ticket.class));
        verify(eventRepository).save(event);
        assertThat(event.getStatus()).isEqualTo(EventStatus.SOLD_OUT);
    }

    @Test // BR-TICKET-009
    void purchase_negativePrice_throwsBusinessRule() {
        stubUser(user(EMAIL, true, ADULT_BIRTH));
        stubEvent(publishedEvent(3));
        stubOccupiedSeats(0);
        when(pricingStrategy.priceFor(TicketType.VIP)).thenReturn(new BigDecimal("-1.00"));

        assertThatThrownBy(() -> service.purchase(request())).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void purchase_missingFields_throwBusinessRule() {
        assertThatThrownBy(() -> service.purchase(new PurchaseTicketRequest(EMAIL, EVENT_CODE, null)))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> service.purchase(new PurchaseTicketRequest(" ", EVENT_CODE, TicketType.VIP)))
                .isInstanceOf(BusinessRuleException.class);
        verifyNoInteractions(userRepository, eventRepository, ticketRepository);
    }

    // ================= cancel

    @Test // TEST-TICKET-009
    void cancel_paidTicket_becomesCancelled() {
        Event event = publishedEvent(3);
        Ticket ticket = ticket("TKT-1", TicketStatus.PAID, user(EMAIL, true, ADULT_BIRTH), event);
        when(ticketRepository.findByTicketCode("TKT-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.toResponse(ticket)).thenReturn(response(TicketStatus.CANCELLED));

        TicketResponse result = service.cancel("TKT-1");

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
        assertThat(result.status()).isEqualTo(TicketStatus.CANCELLED);
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test // TEST-TICKET-010 + BR-TICKET-011
    void cancel_usedOrCancelledTicket_throwsBusinessRule() {
        for (TicketStatus status : List.of(TicketStatus.USED, TicketStatus.CANCELLED)) {
            Ticket ticket = ticket("TKT-1", status, user(EMAIL, true, ADULT_BIRTH), publishedEvent(3));
            when(ticketRepository.findByTicketCode("TKT-1")).thenReturn(Optional.of(ticket));

            assertThatThrownBy(() -> service.cancel("TKT-1")).isInstanceOf(BusinessRuleException.class);
            assertThat(ticket.getStatus()).isEqualTo(status);
        }
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test // BR-TICKET-012
    void cancel_afterEventDate_throwsBusinessRule() {
        Event past = event(EVENT_CODE, EventStatus.PUBLISHED, NOW.minusDays(1), 18, venue("V", 3, true));
        Ticket ticket = ticket("TKT-1", TicketStatus.PAID, user(EMAIL, true, ADULT_BIRTH), past);
        when(ticketRepository.findByTicketCode("TKT-1")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.cancel("TKT-1")).isInstanceOf(BusinessRuleException.class);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.PAID);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void cancel_ticketOfSoldOutEvent_reopensEventSales() {
        Event event = event(EVENT_CODE, EventStatus.SOLD_OUT, EVENT_DATE, 18, venue("V", 3, true));
        Ticket ticket = ticket("TKT-1", TicketStatus.PAID, user(EMAIL, true, ADULT_BIRTH), event);
        when(ticketRepository.findByTicketCode("TKT-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(eventRepository.save(event)).thenReturn(event);
        when(ticketMapper.toResponse(ticket)).thenReturn(response(TicketStatus.CANCELLED));

        service.cancel("TKT-1");

        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        verify(eventRepository).save(event);
    }

    @Test
    void cancel_missingTicket_throwsNotFound() {
        when(ticketRepository.findByTicketCode("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancel("NOPE")).isInstanceOf(ResourceNotFoundException.class);
    }

    // ================= markAsUsed

    @Test // TEST-TICKET-011
    void markAsUsed_paidTicket_becomesUsed() {
        Ticket ticket = ticket("TKT-1", TicketStatus.PAID, user(EMAIL, true, ADULT_BIRTH), publishedEvent(3));
        when(ticketRepository.findByTicketCode("TKT-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        when(ticketMapper.toResponse(ticket)).thenReturn(response(TicketStatus.USED));

        TicketResponse result = service.markAsUsed("TKT-1");

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.USED);
        assertThat(result.status()).isEqualTo(TicketStatus.USED);
    }

    @Test // TEST-TICKET-012 + BR-TICKET-014
    void markAsUsed_cancelledTicket_throwsBusinessRule() {
        Ticket ticket = ticket("TKT-1", TicketStatus.CANCELLED, user(EMAIL, true, ADULT_BIRTH), publishedEvent(3));
        when(ticketRepository.findByTicketCode("TKT-1")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.markAsUsed("TKT-1")).isInstanceOf(BusinessRuleException.class);
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.CANCELLED);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test // BR-TICKET-013
    void markAsUsed_alreadyUsedTicket_throwsBusinessRule() {
        Ticket ticket = ticket("TKT-1", TicketStatus.USED, user(EMAIL, true, ADULT_BIRTH), publishedEvent(3));
        when(ticketRepository.findByTicketCode("TKT-1")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.markAsUsed("TKT-1")).isInstanceOf(BusinessRuleException.class);
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    // ================= consultas

    @Test
    void findByCode_missingTicket_throwsNotFound() {
        when(ticketRepository.findByTicketCode("NOPE")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByCode("NOPE"))
                .isInstanceOf(ResourceNotFoundException.class).hasMessage("Ticket not found: NOPE");
    }

    @Test
    void findByUserEmail_mapsTicketsInRepositoryOrder() {
        Ticket ticket = ticket("TKT-1", TicketStatus.PAID, user(EMAIL, true, ADULT_BIRTH), publishedEvent(3));
        when(ticketRepository.findByUser_EmailIgnoreCaseOrderByPurchaseDateDesc(EMAIL)).thenReturn(List.of(ticket));
        when(ticketMapper.toResponse(ticket)).thenReturn(response(TicketStatus.PAID));

        assertThat(service.findByUserEmail(EMAIL)).hasSize(1);
    }

    @Test
    void findPaidTicketsByEvent_queriesPaidStatus() {
        Ticket ticket = ticket("TKT-1", TicketStatus.PAID, user(EMAIL, true, ADULT_BIRTH), publishedEvent(3));
        when(ticketRepository.findByEvent_EventCodeAndStatus(EVENT_CODE, TicketStatus.PAID)).thenReturn(List.of(ticket));
        when(ticketMapper.toResponse(ticket)).thenReturn(response(TicketStatus.PAID));

        assertThat(service.findPaidTicketsByEvent(EVENT_CODE)).hasSize(1);
    }
}
