package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.Event;
import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.User;
import com.pulsepass.pulsepass.domain.enums.EventStatus;
import com.pulsepass.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.pulsepass.domain.enums.TicketType;
import com.pulsepass.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.pulsepass.dto.response.TicketResponse;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.exception.ResourceNotFoundException;
import com.pulsepass.pulsepass.mapper.TicketMapper;
import com.pulsepass.pulsepass.repository.EventRepository;
import com.pulsepass.pulsepass.repository.TicketRepository;
import com.pulsepass.pulsepass.repository.UserRepository;
import com.pulsepass.pulsepass.service.TicketService;
import com.pulsepass.pulsepass.service.pricing.PricingStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.UUID;

import static com.pulsepass.pulsepass.service.impl.Validations.*;

@Service
@Transactional(readOnly = true)
public class TicketServiceImpl implements TicketService {

    /**
     * Un asiento queda ocupado por un ticket PAID y también por uno USED
     * (ya pagó y entró). Si solo contáramos PAID, marcar un ticket como usado
     * liberaría un cupo que en realidad sigue ocupado.
     */
    private static final List<TicketStatus> SEAT_OCCUPYING_STATUSES =
            List.of(TicketStatus.PAID, TicketStatus.USED);

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final TicketMapper ticketMapper;
    private final PricingStrategy pricingStrategy;
    private final Clock clock;

    public TicketServiceImpl(TicketRepository ticketRepository,
                             UserRepository userRepository,
                             EventRepository eventRepository,
                             TicketMapper ticketMapper,
                             PricingStrategy pricingStrategy,
                             Clock clock) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.ticketMapper = ticketMapper;
        this.pricingStrategy = pricingStrategy;
        this.clock = clock;
    }


    @Override
    @Transactional
    public TicketResponse purchase(PurchaseTicketRequest request) {
        requireRequest(request);
        String email = requireText(request.userEmail(), "userEmail");
        String eventCode = requireText(request.eventCode(), "eventCode");
        TicketType type = requireValue(request.type(), "type");

        // BR-TICKET-001 / 002
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> ResourceNotFoundException.of("User", email));
        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new BusinessRuleException("User is not active: " + email);
        }

        // BR-TICKET-003 / 004 / 005
        Event event = getEvent(eventCode);
        if (event.getStatus() != EventStatus.PUBLISHED) {
            throw new BusinessRuleException(
                    "Tickets can only be purchased for PUBLISHED events. Event "
                            + eventCode + " is " + event.getStatus() + ".");
        }
        if (!event.getEventDate().isAfter(now())) {
            throw new BusinessRuleException("Event has already taken place: " + eventCode);
        }

        // BR-TICKET-006
        validateMinimumAge(user, event);

        // BR-TICKET-007
        int capacity = event.getVenue().getCapacity();
        long occupied = ticketRepository.countByEvent_EventCodeAndStatusIn(
                eventCode, SEAT_OCCUPYING_STATUSES);
        if (occupied >= capacity) {
            throw new BusinessRuleException("Event has no capacity left: " + eventCode);
        }

        // BR-TICKET-009
        BigDecimal price = pricingStrategy.priceFor(type);
        if (price == null || price.signum() < 0) {
            throw new BusinessRuleException("Ticket price cannot be negative.");
        }

        Ticket ticket = Ticket.builder()
                .ticketCode(generateTicketCode())
                .type(type)
                .price(price)
                .status(TicketStatus.PAID) // versión simplificada: sin RESERVED
                .purchaseDate(now())
                .user(user)
                .event(event)
                .build();
        Ticket saved = ticketRepository.save(ticket);

        // BR-TICKET-008: misma transacción
        if (occupied + 1 >= capacity) {
            event.setStatus(EventStatus.SOLD_OUT);
            eventRepository.save(event);
        }

        return ticketMapper.toResponse(saved);
    }

    @Override
    public TicketResponse findByCode(String ticketCode) {
        return ticketMapper.toResponse(getTicket(ticketCode));
    }

    @Override
    public List<TicketResponse> findByUserEmail(String email) {
        return ticketRepository.findByUser_EmailIgnoreCaseOrderByPurchaseDateDesc(email)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    public List<TicketResponse> findPaidTicketsByEvent(String eventCode) {
        return ticketRepository.findByEvent_EventCodeAndStatus(eventCode, TicketStatus.PAID)
                .stream()
                .map(ticketMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TicketResponse cancel(String ticketCode) {
        Ticket ticket = getTicket(ticketCode);

        // BR-TICKET-010 / 011
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException(
                    "Only PAID tickets can be cancelled. Ticket " + ticketCode
                            + " is " + ticket.getStatus() + ".");
        }
        // BR-TICKET-012
        Event event = ticket.getEvent();
        if (!event.getEventDate().isAfter(now())) {
            throw new BusinessRuleException(
                    "Ticket cannot be cancelled after the event date: " + ticketCode);
        }

        ticket.setStatus(TicketStatus.CANCELLED);
        Ticket saved = ticketRepository.save(ticket);

        // Decisión de diseño (no está en el PRD): al liberarse un cupo, un evento
        // SOLD_OUT vuelve a PUBLISHED para poder revender ese asiento.
        if (event.getStatus() == EventStatus.SOLD_OUT) {
            event.setStatus(EventStatus.PUBLISHED);
            eventRepository.save(event);
        }

        return ticketMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TicketResponse markAsUsed(String ticketCode) {
        Ticket ticket = getTicket(ticketCode);

        // BR-TICKET-014
        if (ticket.getStatus() == TicketStatus.CANCELLED) {
            throw new BusinessRuleException("A CANCELLED ticket can never be used: " + ticketCode);
        }
        // BR-TICKET-013
        if (ticket.getStatus() != TicketStatus.PAID) {
            throw new BusinessRuleException(
                    "Only PAID tickets can be marked as used. Ticket " + ticketCode
                            + " is " + ticket.getStatus() + ".");
        }

        ticket.setStatus(TicketStatus.USED);
        return ticketMapper.toResponse(ticketRepository.save(ticket));
    }

    // ---------- helpers

    private void validateMinimumAge(User user, Event event) {
        Integer minimumAge = event.getMinimumAge();
        if (minimumAge == null || minimumAge <= 0) {
            return;
        }
        LocalDate birthDate = user.getProfile() == null ? null : user.getProfile().getBirthDate();
        if (birthDate == null) {
            throw new BusinessRuleException(
                    "Cannot verify minimum age: user has no birth date.");
        }
        // La edad se evalúa en la fecha del evento, no en la de hoy.
        int ageAtEvent = Period.between(birthDate, event.getEventDate().toLocalDate()).getYears();
        if (ageAtEvent < minimumAge) {
            throw new BusinessRuleException("User does not meet minimum age.");
        }
    }

    private Event getEvent(String eventCode) {
        return eventRepository.findByEventCode(eventCode)
                .orElseThrow(() -> ResourceNotFoundException.of("Event", eventCode));
    }

    private Ticket getTicket(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode)
                .orElseThrow(() -> ResourceNotFoundException.of("Ticket", ticketCode));
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private static String generateTicketCode() {
        return "TKT-" + UUID.randomUUID();
    }
}
