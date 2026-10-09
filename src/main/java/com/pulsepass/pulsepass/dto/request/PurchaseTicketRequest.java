package com.pulsepass.pulsepass.dto.request;

import com.pulsepass.pulsepass.domain.enums.TicketType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** El precio NO viene del cliente: lo calcula el sistema según el tipo. */
public record PurchaseTicketRequest(
        @NotBlank(message = "User email is required")
        @Email(message = "User email must be valid")
        String userEmail,

        @NotBlank(message = "Event code is required")
        String eventCode,

        @NotNull(message = "Ticket type is required")
        TicketType type
) {}
