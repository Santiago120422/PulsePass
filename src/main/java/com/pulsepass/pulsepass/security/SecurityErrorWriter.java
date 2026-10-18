package com.pulsepass.pulsepass.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;

/** Escribe el contrato ErrorResponse (6 campos) para errores que ocurren ANTES del DispatcherServlet. */
final class SecurityErrorWriter {

    private SecurityErrorWriter() {}

    static void write(HttpServletResponse response, int status, String error, String message) throws IOException {
        if (response.isCommitted()) {
            return; // la respuesta se emite una sola vez
        }
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        // Mensajes fijos y sin comillas: no hace falta escapar ni depender de ObjectMapper (Jackson 2 vs 3).
        response.getWriter().write("{\"timestamp\":\"" + LocalDateTime.now()
                + "\",\"status\":" + status
                + ",\"error\":\"" + error
                + "\",\"message\":\"" + message
                + "\",\"details\":{}}");
    }
}
