package com.skydiveforecast.infrastructure.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class OpenApiAggregationController {

    private final WebClient webClient = WebClient.builder().build();

    @Value("${USER_SERVICE_URL:http://localhost:8081}")
    private String userServiceUrl;

    @Value("${ANALYSIS_SERVICE_URL:http://localhost:8082}")
    private String analysisServiceUrl;

    @Value("${LOCATION_SERVICE_URL:http://localhost:8083}")
    private String locationServiceUrl;

    @GetMapping(value = "/v3/api-docs/users", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getUsersApiDocs() {
        return webClient.get()
                .uri(userServiceUrl + "/v3/api-docs")
                .retrieve()
                .bodyToMono(String.class)
                .map(this::rewriteServers);
    }

    @GetMapping(value = "/v3/api-docs/analyses", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getAnalysesApiDocs() {
        return webClient.get()
                .uri(analysisServiceUrl + "/v3/api-docs")
                .retrieve()
                .bodyToMono(String.class)
                .map(this::rewriteServers);
    }

    @GetMapping(value = "/v3/api-docs/locations", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getLocationsApiDocs() {
        return webClient.get()
                .uri(locationServiceUrl + "/v3/api-docs")
                .retrieve()
                .bodyToMono(String.class)
                .map(this::rewriteServers);
    }

    private String rewriteServers(String openApiJson) {
        return openApiJson.replaceAll("\"servers\":\\[\\{[^\\]]+\\]", 
                "\"servers\":[{\"url\":\"http://localhost:8080\",\"description\":\"API Gateway\"}]");
    }
}
