package com.wil.reservation_api.security;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "0123456789012345678901234567890123456789";
    private static final long ONE_HOUR_MS = 3_600_000L;
    private static final long ALREADY_EXPIRED_MS = -1_000L;

    private JwtService jwtService(long expirationMillis) {
        return new JwtService(SECRET, expirationMillis);
    }

    @Test
    void givenGeneratedToken_whenExtractUserId_thenReturnsSameUserId() {
        JwtService jwtService = jwtService(ONE_HOUR_MS);
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateToken(userId);

        UUID extracted = jwtService.extractUserId(token);

        assertEquals(userId, extracted);
    }

    @Test
    void givenGeneratedToken_whenIsTokenValid_thenReturnsTrue() {
        JwtService jwtService = jwtService(ONE_HOUR_MS);
        String token = jwtService.generateToken(UUID.randomUUID());

        boolean valid = jwtService.isTokenValid(token);

        assertTrue(valid);
    }

    @Test
    void givenTamperedToken_whenIsTokenValid_thenReturnsFalse() {
        JwtService jwtService = jwtService(ONE_HOUR_MS);
        String tampered = jwtService.generateToken(UUID.randomUUID()) + "tampered";

        boolean valid = jwtService.isTokenValid(tampered);

        assertFalse(valid);
    }

    @Test
    void givenMalformedToken_whenIsTokenValid_thenReturnsFalse() {
        JwtService jwtService = jwtService(ONE_HOUR_MS);

        boolean valid = jwtService.isTokenValid("not-a-valid-token");

        assertFalse(valid);
    }

    @Test
    void givenExpiredToken_whenIsTokenValid_thenReturnsFalse() {
        JwtService jwtService = jwtService(ALREADY_EXPIRED_MS);
        String expired = jwtService.generateToken(UUID.randomUUID());

        boolean valid = jwtService.isTokenValid(expired);

        assertFalse(valid);
    }
}
