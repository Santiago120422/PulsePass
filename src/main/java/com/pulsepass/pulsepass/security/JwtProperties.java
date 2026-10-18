package com.pulsepass.pulsepass.security;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "pulsepass.security.jwt")
public record JwtProperties(
        @NotBlank String issuer,
        @NotBlank String secretBase64,
        Duration accessTokenTtl
) {
    public JwtProperties {
        if (accessTokenTtl == null) {
            accessTokenTtl = Duration.ofMinutes(15);
        }
    }
}
