package com.pulsepass.pulsepass.dto.response;

import com.pulsepass.pulsepass.domain.enums.TicketStatus;
import com.pulsepass.pulsepass.domain.enums.TicketType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TicketResponse(
        Long id,
        String ticketCode,
        TicketType type,
        BigDecimal price,
        TicketStatus status,
        LocalDateTime purchaseDate,
        String userEmail,
        String eventCode,
        String eventName
) {}
