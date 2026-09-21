package com.pulsepass.pulsepass.repository;

import com.pulsepass.pulsepass.domain.Ticket;
import com.pulsepass.pulsepass.domain.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketCode(String ticketCode);

    // FR-TKT-006
    List<Ticket> findByUser_EmailAndStatus(String email, TicketStatus status);
    List<Ticket> findByUser_Email(String email);

    // FR-TKT-007
    List<Ticket> findByEvent_EventCodeAndStatus(String eventCode, TicketStatus status);

    // FR-TKT-008
    long countByEvent_EventCodeAndStatus(String eventCode, TicketStatus status);

    // FR-SRC-004
    @Query("SELECT t FROM Ticket t WHERE t.event.eventDate > :fromDate ORDER BY t.event.eventDate ASC")
    List<Ticket> findTicketsForUpcomingEvents(@Param("fromDate") LocalDateTime fromDate);
}