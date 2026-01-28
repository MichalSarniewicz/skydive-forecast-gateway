package com.skydiveforecast.infrastructure.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtPropertiesTest {

    private static final String VALID_SECRET = "hK8nX2mP9qR5vT3wL7bE4jY6cA1dF8sZ";
    private static final long VALID_EXPIRATION = 3600000L;

    @Test
    void constructor_shouldCreateInstance_whenSecretIsValid() {
        // Arrange
        String secret = VALID_SECRET;
        long expiration = VALID_EXPIRATION;

        // Act
        JwtProperties properties = new JwtProperties(secret, expiration);

        // Assert
        assertEquals(secret, properties.secret());
        assertEquals(expiration, properties.expiration());
    }

    @Test
    void constructor_shouldThrowException_whenSecretIsNull() {
        // Arrange
        String secret = null;
        long expiration = VALID_EXPIRATION;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtProperties(secret, expiration));
        assertEquals("JWT secret must not be blank", exception.getMessage());
    }

    @Test
    void constructor_shouldThrowException_whenSecretIsEmpty() {
        // Arrange
        String secret = "";
        long expiration = VALID_EXPIRATION;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtProperties(secret, expiration));
        assertEquals("JWT secret must not be blank", exception.getMessage());
    }

    @Test
    void constructor_shouldThrowException_whenSecretIsBlank() {
        // Arrange
        String secret = "   ";
        long expiration = VALID_EXPIRATION;

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new JwtProperties(secret, expiration));
        assertEquals("JWT secret must not be blank", exception.getMessage());
    }

    @Test
    void constructor_shouldUseDefaultExpiration_whenExpirationIsZero() {
        // Arrange
        String secret = VALID_SECRET;
        long expiration = 0L;

        // Act
        JwtProperties properties = new JwtProperties(secret, expiration);

        // Assert
        assertEquals(secret, properties.secret());
        assertEquals(3600000L, properties.expiration()); // Default 1 hour
    }

    @Test
    void constructor_shouldUseDefaultExpiration_whenExpirationIsNegative() {
        // Arrange
        String secret = VALID_SECRET;
        long expiration = -1000L;

        // Act
        JwtProperties properties = new JwtProperties(secret, expiration);

        // Assert
        assertEquals(secret, properties.secret());
        assertEquals(3600000L, properties.expiration()); // Default 1 hour
    }

    @Test
    void constructor_shouldPreserveCustomExpiration_whenExpirationIsPositive() {
        // Arrange
        String secret = VALID_SECRET;
        long expiration = 7200000L; // 2 hours

        // Act
        JwtProperties properties = new JwtProperties(secret, expiration);

        // Assert
        assertEquals(secret, properties.secret());
        assertEquals(7200000L, properties.expiration());
    }

    @Test
    void equals_shouldReturnTrue_whenPropertiesAreEqual() {
        // Arrange
        JwtProperties properties1 = new JwtProperties(VALID_SECRET, VALID_EXPIRATION);
        JwtProperties properties2 = new JwtProperties(VALID_SECRET, VALID_EXPIRATION);

        // Act & Assert
        assertEquals(properties1, properties2);
    }

    @Test
    void hashCode_shouldBeEqual_whenPropertiesAreEqual() {
        // Arrange
        JwtProperties properties1 = new JwtProperties(VALID_SECRET, VALID_EXPIRATION);
        JwtProperties properties2 = new JwtProperties(VALID_SECRET, VALID_EXPIRATION);

        // Act & Assert
        assertEquals(properties1.hashCode(), properties2.hashCode());
    }
}
