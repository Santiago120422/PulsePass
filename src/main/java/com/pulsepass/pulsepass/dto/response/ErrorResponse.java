package com.pulsepass.pulsepass.dto.response;

import java.time.LocalDateTime;
import java.util.Map;

/** Contrato JSON uniforme para todos los errores HTTP de la API. */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        Map<String, String> details
) {}
