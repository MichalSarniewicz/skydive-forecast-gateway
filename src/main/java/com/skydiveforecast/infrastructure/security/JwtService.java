package com.skydiveforecast.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final Key signingKey;
    private final JwtProperties jwtProperties;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes());
    }

    public Optional<Claims> validateAndExtractClaims(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            if (claims.getExpiration().before(new Date())) {
                log.debug("JWT token is expired");
                return Optional.empty();
            }

            return Optional.of(claims);
        } catch (ExpiredJwtException e) {
            log.debug("JWT token is expired: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.debug("JWT token is malformed: {}", e.getMessage());
        } catch (SignatureException e) {
            log.debug("JWT signature validation failed: {}", e.getMessage());
        } catch (Exception e) {
            log.debug("JWT token validation failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<String> extractUsername(String token) {
        return validateAndExtractClaims(token)
                .map(Claims::getSubject);
    }

    public Optional<Long> extractUserId(String token) {
        return validateAndExtractClaims(token)
                .map(claims -> claims.get("userId", Long.class));
    }

    @SuppressWarnings("unchecked")
    public Optional<List<String>> extractPermissions(String token) {
        return validateAndExtractClaims(token)
                .map(claims -> claims.get("permissions", List.class));
    }

    @SuppressWarnings("unchecked")
    public Optional<List<String>> extractRoles(String token) {
        return validateAndExtractClaims(token)
                .map(claims -> claims.get("roles", List.class));
    }

    public boolean isTokenValid(String token) {
        return validateAndExtractClaims(token).isPresent();
    }
}
