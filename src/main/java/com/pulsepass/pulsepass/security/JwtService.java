package com.pulsepass.pulsepass.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/** Solo emite y valida JWT. No consulta tickets ni ejecuta negocio. */
@Service
public class JwtService {

    private static final long CLOCK_SKEW_SECONDS = 60;

    private final JwtProperties properties;
    private final SecretKey key;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        // Falla al arrancar si la clave no es Base64 valido o tiene menos de 32 bytes (WeakKeyException).
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secretBase64()));
    }

    /** El token lleva solo el ID persistente (sub) y metadatos; los roles se leen de la BD en cada request. */
    public String generateToken(Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(properties.issuer())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(properties.accessTokenTtl())))
                .id(UUID.randomUUID().toString())
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Verifica firma HS256, issuer y exp, y exige sub, exp, iat y jti.
     * Lanza JwtException o IllegalArgumentException ante cualquier problema (el filtro responde 401).
     */
    public Claims parse(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)                       // rechaza alg=none, tokens sin firma y otros algoritmos
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (claims.getSubject() == null || claims.getSubject().isBlank()
                || claims.getExpiration() == null
                || claims.getId() == null
                || claims.getIssuedAt() == null
                || claims.getIssuedAt().toInstant().isAfter(Instant.now().plusSeconds(CLOCK_SKEW_SECONDS))) {
            throw new JwtException("Missing or invalid required claims");
        }
        return claims;
    }

    public Long extractUserId(Claims claims) {
        return Long.valueOf(claims.getSubject()); // NumberFormatException es IllegalArgumentException
    }

    public long getExpiresInSeconds() {
        return properties.accessTokenTtl().toSeconds();
    }
}
