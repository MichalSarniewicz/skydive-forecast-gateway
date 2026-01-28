package com.skydiveforecast.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private static final String TEST_SECRET = "hK8nX2mP9qR5vT3wL7bE4jY6cA1dF8sZ";
    private static final long TEST_EXPIRATION = 3600000L;
    private static final String TEST_USERNAME = "test@example.com";
    private static final Long TEST_USER_ID = 123L;
    private static final List<String> TEST_PERMISSIONS = List.of("USER_VIEW", "DROPZONE_VIEW");
    private static final List<String> TEST_ROLES = List.of("ADMIN", "USER");

    private JwtService jwtService;
    private Key signingKey;

    @BeforeEach
    void setUp() {
        // Arrange
        JwtProperties jwtProperties = new JwtProperties(TEST_SECRET, TEST_EXPIRATION);
        jwtService = new JwtService(jwtProperties);
        signingKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
    }

    @Test
    void isTokenValid_shouldReturnTrue_whenTokenIsValid() {
        // Arrange
        String validToken = createValidToken();

        // Act
        boolean result = jwtService.isTokenValid(validToken);

        // Assert
        assertTrue(result);
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsExpired() {
        // Arrange
        String expiredToken = createExpiredToken();

        // Act
        boolean result = jwtService.isTokenValid(expiredToken);

        // Assert
        assertFalse(result);
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenIsMalformed() {
        // Arrange
        String malformedToken = "not.a.valid.token";

        // Act
        boolean result = jwtService.isTokenValid(malformedToken);

        // Assert
        assertFalse(result);
    }

    @Test
    void isTokenValid_shouldReturnFalse_whenTokenHasInvalidSignature() {
        // Arrange
        Key wrongKey = Keys.hmacShaKeyFor("wrongSecretKeyThatIsDifferent1234".getBytes()); // 256 bits minimum
        String tokenWithWrongSignature = Jwts.builder()
                .subject(TEST_USERNAME)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TEST_EXPIRATION))
                .signWith(wrongKey)
                .compact();

        // Act
        boolean result = jwtService.isTokenValid(tokenWithWrongSignature);

        // Assert
        assertFalse(result);
    }

    @Test
    void extractUsername_shouldReturnUsername_whenTokenIsValid() {
        // Arrange
        String validToken = createValidToken();

        // Act
        Optional<String> result = jwtService.extractUsername(validToken);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TEST_USERNAME, result.get());
    }

    @Test
    void extractUsername_shouldReturnEmpty_whenTokenIsInvalid() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act
        Optional<String> result = jwtService.extractUsername(invalidToken);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void extractUserId_shouldReturnUserId_whenTokenIsValid() {
        // Arrange
        String validToken = createValidToken();

        // Act
        Optional<Long> result = jwtService.extractUserId(validToken);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TEST_USER_ID, result.get());
    }

    @Test
    void extractUserId_shouldReturnEmpty_whenTokenIsExpired() {
        // Arrange
        String expiredToken = createExpiredToken();

        // Act
        Optional<Long> result = jwtService.extractUserId(expiredToken);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void extractPermissions_shouldReturnPermissions_whenTokenIsValid() {
        // Arrange
        String validToken = createValidToken();

        // Act
        Optional<List<String>> result = jwtService.extractPermissions(validToken);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TEST_PERMISSIONS, result.get());
    }

    @Test
    void extractPermissions_shouldReturnEmpty_whenTokenIsMalformed() {
        // Arrange
        String malformedToken = "malformed.jwt.token";

        // Act
        Optional<List<String>> result = jwtService.extractPermissions(malformedToken);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void extractRoles_shouldReturnRoles_whenTokenIsValid() {
        // Arrange
        String validToken = createValidToken();

        // Act
        Optional<List<String>> result = jwtService.extractRoles(validToken);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TEST_ROLES, result.get());
    }

    @Test
    void extractRoles_shouldReturnEmpty_whenTokenIsInvalid() {
        // Arrange
        String invalidToken = "random.invalid.token";

        // Act
        Optional<List<String>> result = jwtService.extractRoles(invalidToken);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void validateAndExtractClaims_shouldReturnClaims_whenTokenIsValid() {
        // Arrange
        String validToken = createValidToken();

        // Act
        Optional<Claims> result = jwtService.validateAndExtractClaims(validToken);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TEST_USERNAME, result.get().getSubject());
        assertEquals(TEST_USER_ID, result.get().get("userId", Long.class));
    }

    @Test
    void validateAndExtractClaims_shouldReturnEmpty_whenTokenIsEmpty() {
        // Arrange
        String emptyToken = "";

        // Act
        Optional<Claims> result = jwtService.validateAndExtractClaims(emptyToken);

        // Assert
        assertTrue(result.isEmpty());
    }

    // Helper methods to create test tokens

    private String createValidToken() {
        return Jwts.builder()
                .subject(TEST_USERNAME)
                .claim("userId", TEST_USER_ID)
                .claim("permissions", TEST_PERMISSIONS)
                .claim("roles", TEST_ROLES)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + TEST_EXPIRATION))
                .signWith(signingKey)
                .compact();
    }

    private String createExpiredToken() {
        return Jwts.builder()
                .subject(TEST_USERNAME)
                .claim("userId", TEST_USER_ID)
                .claim("permissions", TEST_PERMISSIONS)
                .claim("roles", TEST_ROLES)
                .issuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
                .expiration(new Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
                .signWith(signingKey)
                .compact();
    }
}
