package com.skydiveforecast.infrastructure.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.security.Key;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String TEST_SECRET = "hK8nX2mP9qR5vT3wL7bE4jY6cA1dF8sZ";
    private static final String TEST_USERNAME = "test@example.com";
    private static final Long TEST_USER_ID = 123L;
    private static final List<String> TEST_PERMISSIONS = List.of("USER_VIEW", "DROPZONE_VIEW");
    private static final List<String> TEST_ROLES = List.of("ADMIN");

    @Mock
    private WebFilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private JwtService jwtService;
    private Key signingKey;

    @BeforeEach
    void setUp() {
        // Arrange
        JwtProperties jwtProperties = new JwtProperties(TEST_SECRET, 3600000L);
        jwtService = new JwtService(jwtProperties);
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtService);
        signingKey = Keys.hmacShaKeyFor(TEST_SECRET.getBytes());
    }

    @Test
    void filter_shouldPassThrough_whenPathIsPublic() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/auth/token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        verify(filterChain, times(1)).filter(any());
    }

    @Test
    void filter_shouldPassThrough_whenPathIsActuator() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/actuator/health")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        verify(filterChain, times(1)).filter(any());
    }

    @Test
    void filter_shouldPassThrough_whenPathIsSwagger() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/swagger-ui.html")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        verify(filterChain, times(1)).filter(any());
    }

    @Test
    void filter_shouldReturnUnauthorized_whenAuthorizationHeaderIsMissing() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/me")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void filter_shouldReturnUnauthorized_whenAuthorizationHeaderDoesNotStartWithBearer() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void filter_shouldReturnUnauthorized_whenTokenIsEmpty() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer ")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void filter_shouldReturnUnauthorized_whenTokenFormatIsInvalid() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token-without-dots")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void filter_shouldReturnUnauthorized_whenTokenIsExpired() {
        // Arrange
        String expiredToken = createExpiredToken();
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
        verify(filterChain, never()).filter(any());
    }

    @Test
    void filter_shouldContinueChain_whenTokenIsValid() {
        // Arrange
        String validToken = createValidToken();
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        verify(filterChain, times(1)).filter(any());
    }

    @Test
    void filter_shouldAddUserHeadersToRequest_whenTokenIsValid() {
        // Arrange
        String validToken = createValidToken();
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/users/me")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + validToken)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(argThat(ex -> {
            // Verify headers are added to the mutated request
            var mutatedRequest = ex.getRequest();
            return mutatedRequest.getHeaders().containsKey("X-User-Email") &&
                    mutatedRequest.getHeaders().containsKey("X-User-Id") &&
                    mutatedRequest.getHeaders().containsKey("X-User-Permissions") &&
                    mutatedRequest.getHeaders().containsKey("X-User-Roles");
        }))).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        verify(filterChain, times(1)).filter(any());
    }

    @Test
    void filter_shouldPassThrough_whenPathIsApiDocsWithWildcard() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/v3/api-docs/users")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        verify(filterChain, times(1)).filter(any());
    }

    @Test
    void filter_shouldPassThrough_whenPathIsWebjars() {
        // Arrange
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/webjars/swagger-ui/index.css")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(filterChain.filter(any())).thenReturn(Mono.empty());

        // Act
        Mono<Void> result = jwtAuthenticationFilter.filter(exchange, filterChain);

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
        verify(filterChain, times(1)).filter(any());
    }

    // Helper methods

    private String createValidToken() {
        return Jwts.builder()
                .subject(TEST_USERNAME)
                .claim("userId", TEST_USER_ID)
                .claim("permissions", TEST_PERMISSIONS)
                .claim("roles", TEST_ROLES)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(signingKey)
                .compact();
    }

    private String createExpiredToken() {
        return Jwts.builder()
                .subject(TEST_USERNAME)
                .claim("userId", TEST_USER_ID)
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(signingKey)
                .compact();
    }
}
