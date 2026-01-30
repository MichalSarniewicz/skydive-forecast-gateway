package com.skydiveforecast.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springdoc.core.models.GroupedOpenApi;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        // Arrange
        openApiConfig = new OpenApiConfig();
    }

    @Test
    void customOpenAPI_shouldReturnOpenAPIWithCorrectTitle() {
        // Act
        OpenAPI result = openApiConfig.customOpenAPI();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getInfo());
        assertEquals("Skydive Forecast Gateway API", result.getInfo().getTitle());
    }

    @Test
    void customOpenAPI_shouldReturnOpenAPIWithCorrectVersion() {
        // Act
        OpenAPI result = openApiConfig.customOpenAPI();

        // Assert
        assertEquals("1.0", result.getInfo().getVersion());
    }

    @Test
    void customOpenAPI_shouldReturnOpenAPIWithDescription() {
        // Act
        OpenAPI result = openApiConfig.customOpenAPI();

        // Assert
        assertNotNull(result.getInfo().getDescription());
        assertTrue(result.getInfo().getDescription().contains("Unified API Gateway"));
    }

    @Test
    void customOpenAPI_shouldHaveBearerAuthSecurityScheme() {
        // Act
        OpenAPI result = openApiConfig.customOpenAPI();

        // Assert
        assertNotNull(result.getComponents());
        assertNotNull(result.getComponents().getSecuritySchemes());
        assertTrue(result.getComponents().getSecuritySchemes().containsKey("BearerAuth"));
    }

    @Test
    void customOpenAPI_shouldHaveSecurityRequirement() {
        // Act
        OpenAPI result = openApiConfig.customOpenAPI();

        // Assert
        assertNotNull(result.getSecurity());
        assertFalse(result.getSecurity().isEmpty());
    }

    @Test
    void apis_shouldReturnThreeApiGroups() {
        // Act
        List<GroupedOpenApi> result = openApiConfig.apis();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
    }

    @Test
    void apis_shouldContainUsersGroup() {
        // Act
        List<GroupedOpenApi> result = openApiConfig.apis();

        // Assert
        boolean hasUsersGroup = result.stream()
                .anyMatch(api -> "users".equals(api.getGroup()));
        assertTrue(hasUsersGroup);
    }

    @Test
    void apis_shouldContainAnalysesGroup() {
        // Act
        List<GroupedOpenApi> result = openApiConfig.apis();

        // Assert
        boolean hasAnalysesGroup = result.stream()
                .anyMatch(api -> "analyses".equals(api.getGroup()));
        assertTrue(hasAnalysesGroup);
    }

    @Test
    void apis_shouldContainLocationsGroup() {
        // Act
        List<GroupedOpenApi> result = openApiConfig.apis();

        // Assert
        boolean hasLocationsGroup = result.stream()
                .anyMatch(api -> "locations".equals(api.getGroup()));
        assertTrue(hasLocationsGroup);
    }

    @Test
    void customOpenAPI_bearerAuthSchemeShouldBeHttpType() {
        // Act
        OpenAPI result = openApiConfig.customOpenAPI();
        var bearerAuth = result.getComponents().getSecuritySchemes().get("BearerAuth");

        // Assert
        assertNotNull(bearerAuth);
        assertEquals(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP, bearerAuth.getType());
        assertEquals("bearer", bearerAuth.getScheme());
        assertEquals("JWT", bearerAuth.getBearerFormat());
    }
}
