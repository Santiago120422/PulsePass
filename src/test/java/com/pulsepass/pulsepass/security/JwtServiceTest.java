package com.pulsepass.pulsepass.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String KEY = "x2XCGrSYLyn/ytOPS1jxM5uNb+7nKc9/Dy6sphYoYBI=";
    private static final String ISSUER = "pulsepass-api";

    private final JwtService service = new JwtService(new JwtProperties(ISSUER, KEY, Duration.ofMinutes(15)));
    private final SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(KEY));

    @Test
    void generatedToken_hasExpectedClaims() {
        Claims claims = service.parse(service.generateToken(42L));

        assertEquals("42", claims.getSubject());
        assertEquals(ISSUER, claims.getIssuer());
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        long ttl = (claims.getExpiration().getTime() - claims.getIssuedAt().getTime()) / 1000;
        assertEquals(900, ttl);
        assertEquals(42L, service.extractUserId(claims));
        assertEquals(900, service.getExpiresInSeconds());
        assertNull(claims.get("roles"), "los roles no viajan en el token");
    }

    @Test
    void expiredToken_isRejected() {
        Instant past = Instant.now().minusSeconds(3600);
        String token = Jwts.builder().subject("1").issuer(ISSUER).id(UUID.randomUUID().toString())
                .issuedAt(Date.from(past)).expiration(Date.from(past.plusSeconds(60)))
                .signWith(key, Jwts.SIG.HS256).compact();
        assertThrows(JwtException.class, () -> service.parse(token));
    }

    @Test
    void tamperedToken_isRejected() {
        String token = service.generateToken(1L);
        int i = token.indexOf('.') + 5;                       // un caracter del payload
        char c = token.charAt(i) == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, i) + c + token.substring(i + 1);
        assertThrows(JwtException.class, () -> service.parse(tampered));
    }

    @Test
    void wrongIssuer_isRejected() {
        JwtService other = new JwtService(new JwtProperties("otro-emisor", KEY, Duration.ofMinutes(15)));
        assertThrows(JwtException.class, () -> service.parse(other.generateToken(1L)));
    }

    @Test
    void tokenSignedWithAnotherKey_isRejected() {
        SecretKey otherKey = Jwts.SIG.HS256.key().build();
        Instant now = Instant.now();
        String token = Jwts.builder().subject("1").issuer(ISSUER).id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(60)))
                .signWith(otherKey, Jwts.SIG.HS256).compact();
        assertThrows(JwtException.class, () -> service.parse(token));
    }

    @Test
    void unsignedToken_isRejected() {
        Instant now = Instant.now();
        String unsigned = Jwts.builder().subject("1").issuer(ISSUER).id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(60))).compact();
        assertThrows(JwtException.class, () -> service.parse(unsigned));
    }

    @Test
    void missingJti_isRejected() {
        Instant now = Instant.now();
        String token = Jwts.builder().subject("1").issuer(ISSUER)
                .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(60)))
                .signWith(key, Jwts.SIG.HS256).compact();
        assertThrows(JwtException.class, () -> service.parse(token));
    }

    @Test
    void emptyOrMalformedToken_isRejected() {
        assertThrows(RuntimeException.class, () -> service.parse(""));
        assertThrows(RuntimeException.class, () -> service.parse("no.es.un.jwt"));
    }

    @Test
    void weakKey_failsAtStartup() {
        String weak = java.util.Base64.getEncoder().encodeToString(new byte[16]);
        assertThrows(RuntimeException.class,
                () -> new JwtService(new JwtProperties(ISSUER, weak, Duration.ofMinutes(15))));
    }
}
