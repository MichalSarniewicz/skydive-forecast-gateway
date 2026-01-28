package com.skydiveforecast.infrastructure.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.test.StepVerifier;

import java.net.InetSocketAddress;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class RateLimiterConfigTest {

    private RateLimiterConfig rateLimiterConfig;
    private KeyResolver keyResolver;

    @BeforeEach
    void setUp() {
        // Arrange
        rateLimiterConfig = new RateLimiterConfig();
        keyResolver = rateLimiterConfig.ipKeyResolver();
    }

    @Test
    void ipKeyResolver_shouldReturnXForwardedForHeader_whenPresent() {
        // Arrange
        String expectedIp = "192.168.1.100";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .header("X-Forwarded-For", expectedIp)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(keyResolver.resolve(exchange))
                .expectNext(expectedIp)
                .verifyComplete();
    }

    @Test
    void ipKeyResolver_shouldReturnFirstIp_whenXForwardedForContainsMultipleIps() {
        // Arrange
        String expectedIp = "192.168.1.100";
        String xForwardedFor = "192.168.1.100, 10.0.0.1, 172.16.0.1";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .header("X-Forwarded-For", xForwardedFor)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(keyResolver.resolve(exchange))
                .expectNext(expectedIp)
                .verifyComplete();
    }

    @Test
    void ipKeyResolver_shouldReturnTrimmedIp_whenXForwardedForHasWhitespace() {
        // Arrange
        String expectedIp = "192.168.1.100";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .header("X-Forwarded-For", "  192.168.1.100  ")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(keyResolver.resolve(exchange))
                .expectNext(expectedIp)
                .verifyComplete();
    }

    @Test
    void ipKeyResolver_shouldReturnRemoteAddress_whenXForwardedForIsMissing() {
        // Arrange
        String expectedIp = "127.0.0.1";
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .remoteAddress(new InetSocketAddress(expectedIp, 12345))
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(keyResolver.resolve(exchange))
                .expectNext(expectedIp)
                .verifyComplete();
    }

    @Test
    void ipKeyResolver_shouldReturnUnknown_whenNoAddressAvailable() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(keyResolver.resolve(exchange))
                .expectNext("unknown")
                .verifyComplete();
    }

    @Test
    void ipKeyResolver_shouldNotReturnNull() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/test")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act & Assert
        StepVerifier.create(keyResolver.resolve(exchange))
                .assertNext(Assertions::assertNotNull)
                .verifyComplete();
    }

    @Test
    void ipKeyResolver_shouldBeCreatedAsBean() {
        // Arrange & Act
        KeyResolver resolver = rateLimiterConfig.ipKeyResolver();

        // Assert
        assertNotNull(resolver);
    }
}
