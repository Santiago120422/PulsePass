package com.pulsepass.pulsepass.dto.response;

import com.pulsepass.pulsepass.domain.enums.EventCategory;
import com.pulsepass.pulsepass.domain.enums.EventStatus;

import java.time.LocalDateTime;

public record EventSummaryResponse(
        Long id,
        String eventCode,
        String name,
        EventCategory category,
        EventStatus status,
        LocalDateTime eventDate,
        String venueCode,
        String venueName
) {}
