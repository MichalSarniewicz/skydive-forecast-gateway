package com.skydiveforecast.infrastructure.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/users/auth/**",
            "/actuator/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/webjars/**");

    private final JwtService jwtService;
    private final AntPathMatcher pathMatcher;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
        this.pathMatcher = new AntPathMatcher();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // Skip authentication for public paths
        if (isPublicPath(path)) {
            log.debug("Skipping JWT authentication for public path: {}", path);
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.debug("Missing or invalid Authorization header for path: {}", path);
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        if (token.isEmpty() || token.chars().filter(ch -> ch == '.').count() < 2) {
            log.debug("Invalid JWT token format for path: {}", path);
            return unauthorized(exchange, "Invalid JWT token format");
        }

        if (!jwtService.isTokenValid(token)) {
            log.debug("JWT token validation failed for path: {}", path);
            return unauthorized(exchange, "Invalid or expired JWT token");
        }

        // Token is valid - extract user info and add to headers for downstream services
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-User-Email", jwtService.extractUsername(token).orElse(""))
                .header("X-User-Id", jwtService.extractUserId(token).map(String::valueOf).orElse(""))
                .header("X-User-Permissions", String.join(",", jwtService.extractPermissions(token).orElse(List.of())))
                .header("X-User-Roles", String.join(",", jwtService.extractRoles(token).orElse(List.of())))
                .build();

        log.debug("JWT authentication successful for user: {}", jwtService.extractUsername(token).orElse("unknown"));

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        String body = String.format("{\"error\":\"Unauthorized\",\"message\":\"%s\"}", message);
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(body.getBytes())));
    }
}
