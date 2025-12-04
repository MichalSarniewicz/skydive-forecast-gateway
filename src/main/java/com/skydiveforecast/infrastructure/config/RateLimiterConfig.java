package com.skydiveforecast.infrastructure.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = Optional.ofNullable(exchange.getRequest().getHeaders().getFirst("X-Forwarded-For"))
                    .map(header -> header.split(",")[0].trim())
                    .orElseGet(() -> Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                            .map(addr -> addr.getAddress().getHostAddress())
                            .orElse("unknown"));
            return Mono.just(ip);
        };
    }
}
