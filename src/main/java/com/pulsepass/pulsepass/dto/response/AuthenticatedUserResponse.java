package com.pulsepass.pulsepass.dto.response;

import java.util.List;

/** Informacion minima del principal. Sin email, hash ni secretos. */
public record AuthenticatedUserResponse(Long id, String username, List<String> roles) {}
