package com.skydiveforecast.infrastructure.adapter;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenApiAggregationControllerTest {

    private MockWebServer mockWebServer;
    private OpenApiAggregationController controller;

    @BeforeEach
    void setUp() throws IOException {
        // Arrange
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        controller = new OpenApiAggregationController();
        String baseUrl = mockWebServer.url("/").toString();
        // Remove trailing slash
        baseUrl = baseUrl.substring(0, baseUrl.length() - 1);

        ReflectionTestUtils.setField(controller, "userServiceUrl", baseUrl);
        ReflectionTestUtils.setField(controller, "analysisServiceUrl", baseUrl);
        ReflectionTestUtils.setField(controller, "locationServiceUrl", baseUrl);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void getUsersApiDocs_shouldReturnApiDocs_whenServiceResponds() {
        // Arrange - use single line JSON to match regex
        String mockResponse = "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"Users API\"},\"servers\":[{\"url\":\"http://localhost:8081\",\"description\":\"User Service\"}],\"paths\":{}}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        StepVerifier.create(controller.getUsersApiDocs())
                .assertNext(response -> {
                    assertTrue(response.contains("openapi"));
                    assertTrue(response.contains("API Gateway"));
                })
                .verifyComplete();
    }

    @Test
    void getAnalysesApiDocs_shouldReturnApiDocs_whenServiceResponds() {
        // Arrange - use single line JSON
        String mockResponse = "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"Analyses API\"},\"servers\":[{\"url\":\"http://localhost:8082\"}],\"paths\":{}}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        StepVerifier.create(controller.getAnalysesApiDocs())
                .assertNext(response -> {
                    assertTrue(response.contains("openapi"));
                    assertTrue(response.contains("API Gateway"));
                })
                .verifyComplete();
    }

    @Test
    void getLocationsApiDocs_shouldReturnApiDocs_whenServiceResponds() {
        // Arrange - use single line JSON
        String mockResponse = "{\"openapi\":\"3.0.1\",\"info\":{\"title\":\"Locations API\"},\"servers\":[{\"url\":\"http://localhost:8083\"}],\"paths\":{}}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        StepVerifier.create(controller.getLocationsApiDocs())
                .assertNext(response -> {
                    assertTrue(response.contains("openapi"));
                    assertTrue(response.contains("API Gateway"));
                })
                .verifyComplete();
    }

    @Test
    void getUsersApiDocs_shouldReturnErrorDoc_whenServiceReturns500() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Act & Assert
        StepVerifier.create(controller.getUsersApiDocs())
                .assertNext(response -> {
                    assertTrue(response.contains("Unavailable"));
                    assertTrue(response.contains("error"));
                })
                .verifyComplete();
    }

    @Test
    void getAnalysesApiDocs_shouldReturnErrorDoc_whenServiceReturns404() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("Not Found"));

        // Act & Assert
        StepVerifier.create(controller.getAnalysesApiDocs())
                .assertNext(response -> {
                    assertTrue(response.contains("Unavailable"));
                    assertTrue(response.contains("analyses"));
                })
                .verifyComplete();
    }

    @Test
    void getLocationsApiDocs_shouldReturnErrorDoc_whenServiceIsUnavailable() throws IOException {
        // Arrange - shutdown server to simulate unavailable service
        mockWebServer.shutdown();

        // Act & Assert
        StepVerifier.create(controller.getLocationsApiDocs())
                .assertNext(response -> {
                    assertTrue(response.contains("Unavailable"));
                    assertTrue(response.contains("locations"));
                })
                .verifyComplete();
    }

    @Test
    void getUsersApiDocs_shouldRewriteServers_toGatewayUrl() {
        // Arrange - single line JSON for regex match
        String mockResponse = "{\"openapi\":\"3.0.1\",\"servers\":[{\"url\":\"http://localhost:8081\",\"description\":\"User Service\"}],\"paths\":{}}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        // Act & Assert
        StepVerifier.create(controller.getUsersApiDocs())
                .assertNext(response -> {
                    assertTrue(response.contains("http://localhost:8080"));
                    assertTrue(response.contains("API Gateway"));
                })
                .verifyComplete();
    }
}
