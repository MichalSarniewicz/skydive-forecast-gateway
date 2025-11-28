package com.skydiveforecast.infrastructure.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class OpenApiAggregationController {

    private static final Logger log = LoggerFactory.getLogger(OpenApiAggregationController.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final WebClient webClient = WebClient.builder().build();

    @Value("${USER_SERVICE_URL:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${ANALYSIS_SERVICE_URL:http://localhost:8082}")
    private String analysisServiceUrl;

    @Value("${LOCATION_SERVICE_URL:http://localhost:8083}")
    private String locationServiceUrl;

    @GetMapping(value = "/v3/api-docs/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getUsersApiDocs() {
        return fetchApiDocsFromPath(userServiceUrl, "/v3/api-docs", "users");
    }

    @GetMapping(value = "/v3/api-docs/analyses", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getAnalysesApiDocs() {
        return fetchApiDocsFromPath(analysisServiceUrl, "/v3/api-docs/analyses", "analyses");
    }

    @GetMapping(value = "/v3/api-docs/locations", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getLocationsApiDocs() {
        return fetchApiDocsFromPath(locationServiceUrl, "/v3/api-docs/locations", "locations");
    }

    private Mono<String> fetchApiDocsFromPath(String serviceUrl, String path, String serviceName) {
        return webClient.get()
                .uri(serviceUrl + path)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(TIMEOUT)
                .map(this::rewriteServers)
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.error("Error fetching API docs for {}: {} - {}", serviceName, e.getStatusCode(), e.getMessage());
                    return Mono.just(createErrorApiDoc(serviceName, "Service returned error: " + e.getStatusCode()));
                })
                .onErrorResume(Exception.class, e -> {
                    log.error("Error fetching API docs for {}: {}", serviceName, e.getMessage());
                    return Mono.just(createErrorApiDoc(serviceName, "Service unavailable: " + e.getMessage()));
                });
    }

    private String createErrorApiDoc(String serviceName, String errorMessage) {
        return String.format("""
                {
                    "openapi": "3.0.1",
                    "info": {
                        "title": "%s API (Unavailable)",
                        "description": "%s",
                        "version": "1.0"
                    },
                    "servers": [{"url": "http://localhost:8080", "description": "API Gateway"}],
                    "paths": {}
                }
                """, serviceName, errorMessage);
    }

    private String rewriteServers(String openApiJson) {
        return openApiJson.replaceAll("\"servers\":\\[\\{[^\\]]+\\]", 
                "\"servers\":[{\"url\":\"http://localhost:8080\",\"description\":\"API Gateway\"}]");
    }
}
