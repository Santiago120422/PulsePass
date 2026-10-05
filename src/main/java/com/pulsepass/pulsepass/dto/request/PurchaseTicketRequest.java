package com.pulsepass.pulsepass.dto.request;

import com.pulsepass.pulsepass.domain.enums.TicketType;

/** El precio NO viene del cliente: lo calcula el sistema según el tipo. */
public record PurchaseTicketRequest(
        String userEmail,
        String eventCode,
        TicketType type
) {}
