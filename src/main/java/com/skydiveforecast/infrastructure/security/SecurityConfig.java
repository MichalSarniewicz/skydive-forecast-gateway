package com.skydiveforecast.infrastructure.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {
    // JWT authentication is handled by JwtAuthenticationFilter (WebFilter)
    // This configuration enables JwtProperties to be loaded from application config
}
