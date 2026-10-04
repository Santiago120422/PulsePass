package com.pulsepass.pulsepass.service.impl;

import com.pulsepass.pulsepass.domain.*;
import com.pulsepass.pulsepass.domain.enums.*;
import com.pulsepass.pulsepass.dto.request.PurchaseTicketRequest;
import com.pulsepass.pulsepass.dto.response.TicketResponse;
import com.pulsepass.pulsepass.exception.BusinessRuleException;
import com.pulsepass.pulsepass.mapper.TicketMapper;
import com.pulsepass.pulsepass.repository.*;
import com.pulsepass.pulsepass.service.pricing.DefaultPricingStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.pulsepass.pulsepass.service.impl.TestFixtures.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

/**
 * Escenario principal del PRD (sección 47/48) con un "repositorio en memoria"
 * simulado con Mockito: capacidad 3, evento +18.
 */
@ExtendWith(MockitoExtension.class)
class TicketPurchaseScenarioTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private UserRepository userRepository;
    @Mock private EventRepository eventRepository;
    @Mock private TicketMapper ticketMapper;

    private final List<Ticket> stored = new ArrayList<>();
    private TicketServiceImpl service;
    private Event event;

    @BeforeEach
    void setUp() {
        Venue venue = venue("VEN-SMR-01", 3, true);
        event = event("CMF-2026", EventStatus.PUBLISHED, NOW.plusDays(60), 18, venue);

        lenient().when(eventRepository.findByEventCode("CMF-2026")).thenReturn(Optional.of(event));
        lenient().when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(ticketRepository.save(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            if (!stored.contains(t)) {
                stored.add(t);
            }
            return t;
        });
        lenient().when(ticketRepository.countByEvent_EventCodeAndStatusIn(anyString(), anyCollection()))
                .thenAnswer(inv -> (long) stored.stream()
                        .filter(t -> t.getStatus() == TicketStatus.PAID || t.getStatus() == TicketStatus.USED)
                        .count());
        lenient().when(ticketMapper.toResponse(any(Ticket.class))).thenAnswer(inv -> {
            Ticket t = inv.getArgument(0);
            return new TicketResponse(t.getId(), t.getTicketCode(), t.getType(), t.getPrice(), t.getStatus(),
                    t.getPurchaseDate(), t.getUser().getEmail(), t.getEvent().getEventCode(), t.getEvent().getName());
        });

        service = new TicketServiceImpl(ticketRepository, userRepository, eventRepository, ticketMapper,
                new DefaultPricingStrategy(new BigDecimal("100000.00")), CLOCK);
    }

    private void registerUser(String email, boolean active, LocalDate birthDate) {
        lenient().when(userRepository.findByEmailIgnoreCase(email))
                .thenReturn(Optional.of(user(email, active, birthDate)));
    }

    private TicketResponse buy(String email, TicketType type) {
        return service.purchase(new PurchaseTicketRequest(email, "CMF-2026", type));
    }

    @Test
    void fullAcceptanceScenario() {
        registerUser("andrea@email.com", true, LocalDate.of(2001, 1, 1));  // 25 en el evento
        registerUser("carlos@email.com", true, LocalDate.of(2005, 1, 1));  // 21
        registerUser("laura@email.com", true, LocalDate.of(2009, 6, 1));   // 17
        registerUser("miguel@email.com", false, LocalDate.of(1996, 1, 1)); // inactivo
        registerUser("sofia@email.com", true, LocalDate.of(1998, 1, 1));   // tercer adulto

        // AC-004: Andrea (VIP = 2x precio base)
        TicketResponse andrea = buy("andrea@email.com", TicketType.VIP);
        assertThat(andrea.status()).isEqualTo(TicketStatus.PAID);
        assertThat(andrea.price()).isEqualByComparingTo("200000.00");

        // AC-005: Carlos
        assertThat(buy("carlos@email.com", TicketType.GENERAL).status()).isEqualTo(TicketStatus.PAID);

        // AC-006: Laura (17) rechazada por edad
        assertThatThrownBy(() -> buy("laura@email.com", TicketType.GENERAL))
                .isInstanceOf(BusinessRuleException.class).hasMessage("User does not meet minimum age.");

        // AC-007: Miguel inactivo
        assertThatThrownBy(() -> buy("miguel@email.com", TicketType.GENERAL))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("not active");
        assertThat(stored).hasSize(2);
        assertThat(event.getStatus()).isEqualTo(EventStatus.PUBLISHED);

        // AC-008: último ticket → SOLD_OUT
        assertThat(buy("sofia@email.com", TicketType.STUDENT).status()).isEqualTo(TicketStatus.PAID);
        assertThat(event.getStatus()).isEqualTo(EventStatus.SOLD_OUT);

        // AC-009: cuarta compra rechazada
        assertThatThrownBy(() -> buy("andrea@email.com", TicketType.GENERAL))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(stored).hasSize(3);

        // AC-010 / AC-011
        lenient().when(ticketRepository.findByTicketCode(andrea.ticketCode()))
                .thenReturn(Optional.of(stored.get(0)));
        assertThat(service.markAsUsed(andrea.ticketCode()).status()).isEqualTo(TicketStatus.USED);
        assertThatThrownBy(() -> service.cancel(andrea.ticketCode()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void usedTicketStillOccupiesItsSeat() {
        registerUser("andrea@email.com", true, LocalDate.of(2001, 1, 1));
        registerUser("carlos@email.com", true, LocalDate.of(2005, 1, 1));
        registerUser("sofia@email.com", true, LocalDate.of(1998, 1, 1));

        TicketResponse first = buy("andrea@email.com", TicketType.GENERAL);
        buy("carlos@email.com", TicketType.GENERAL);
        lenient().when(ticketRepository.findByTicketCode(first.ticketCode())).thenReturn(Optional.of(stored.get(0)));
        service.markAsUsed(first.ticketCode()); // ya no es PAID, pero sigue ocupando asiento

        buy("sofia@email.com", TicketType.GENERAL); // 3er y último asiento

        assertThat(event.getStatus()).isEqualTo(EventStatus.SOLD_OUT);
    }
}
