# Skydive Forecast - Gateway

[![CI Pipeline](https://github.com/MichalSarniewicz/skydive-forecast-gateway/actions/workflows/ci.yml/badge.svg)](https://github.com/MichalSarniewicz/skydive-forecast-gateway/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/MichalSarniewicz/skydive-forecast-gateway/branch/master/graph/badge.svg)](https://codecov.io/gh/MichalSarniewicz/skydive-forecast-gateway)
[![Java](https://img.shields.io/badge/Java-21-green?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)


API Gateway for the Skydive Forecast microservices architecture, built with Spring Cloud Gateway and OpenAPI documentation aggregation.

## Overview

This project serves as the central entry point for the Skydive Forecast system, routing requests to various microservices and providing unified API documentation through Swagger UI.

## Architecture

The gateway routes requests to the following microservices:

- **Users Service** (Port 8081): User management and authentication
- **Analyses Service** (Port 8082): Skydive forecast analysis operations
- **Locations Service** (Port 8083): Location and dropzone management

## Technology Stack

- **Java**: 21
- **Spring Boot**: 3.5.6
- **Spring Cloud**: 2025.0.0
- **Spring Cloud Gateway**: WebFlux
- **Spring Cloud Config Client**: Centralized configuration
- **Spring Cloud Consul Discovery**: Service discovery and registration
- **SpringDoc OpenAPI**: 2.8.13 (Aggregated Swagger UI)
- **Monitoring**: Actuator, Prometheus, Grafana, Loki, Tempo
- **Build Tool**: Maven

## Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.x

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd skydive-forecast-gateway
```

2. Build the project:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

The gateway will start on port **8080** by default.

## Configuration

The application supports multiple profiles for different environments:

- `dev` (default): Development environment
- `test`: Testing environment
- `prod`: Production environment

### Service Discovery with Consul

The gateway uses Consul for automatic service discovery. Services are discovered using the `lb://` protocol:

- Users Service: `lb://user-service`
- Analyses Service: `lb://analysis-service`
- Locations Service: `lb://location-service`

No hardcoded URLs needed - services register themselves in Consul and are automatically discovered.

### Configuration Management

Configuration is loaded from Spring Cloud Config Server:
- **Config Server**: `http://config-server:8888`
- **Profiles**: `dev`, `consul`, `swagger`
- Configuration files stored in Git repository

To run with a specific profile:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

Or set in `application.yaml`:
```yaml
spring:
  profiles:
    active: prod
```

## API Routes

### Microservice Routes

| Path | Microservice | Port |
|------|-------------|------|
| `/api/v1/users/**` | Users Service | 8081 |
| `/api/v1/analyses/**` | Analyses Service | 8082 |
| `/api/v1/locations/**` | Locations Service | 8083 |

**Note**: The gateway strips the first 3 path segments (`/api/v1/{service}`) before forwarding to microservices.

### API Documentation

- **Swagger UI**: `http://localhost:8080/swagger-ui.html` (Aggregated from all services)
- **Consul UI**: `http://localhost:8500` (Service registry and health checks)

The Swagger UI provides a unified interface for all microservices APIs through the gateway routing.

## Security

The API uses JWT Bearer token authentication. Include the token in the Authorization header:

```
Authorization: Bearer <your-jwt-token>
```

## Project Structure

```
skydive-forecast-gateway/
├── src/
│   ├── main/
│   │   ├── java/com/skydiveforecast/
│   │   │   ├── Application.java
│   │   │   └── infrastructure/
│   │   │       ├── adapter/
│   │   │       └── config/
│   │   │           └── OpenApiConfig.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── application-dev.yaml
│   │       ├── application-test.yaml
│   │       ├── application-prod.yaml
│   │       └── logback-spring.xml
│   └── test/
├── Dockerfile
├── pom.xml
└── README.md
```

## Development

### Prerequisites

Before running the gateway, ensure these services are running:
1. **Consul** (Port 8500) - Service discovery
2. **Config Server** (Port 8888) - Configuration management
3. **Microservices** (Ports 8081-8083) - Backend services

### Building

```bash
mvn clean package
```

### Running Locally

Update `bootstrap.yaml` to use localhost:
```yaml
spring:
  cloud:
    config:
      uri: http://localhost:8888
    consul:
      host: localhost
```

Then run:
```bash
java -jar target/skydive-forecast-gateway-1.0.0-SNAPSHOT.jar
```

### Docker

Build and run using Docker:

```bash
# Build the Docker image
docker build -t skydive-forecast-gateway:latest .

# Run the container
docker run -p 8080:8080 \
  -e USER_SERVICE_URL=http://users-service:8081 \
  -e ANALYSIS_SERVICE_URL=http://analyses-service:8082 \
  -e LOCATION_SERVICE_URL=http://locations-service:8083 \
  skydive-forecast-gateway:latest
```

## Monitoring

The service includes comprehensive monitoring capabilities:

### Metrics (Prometheus)

- **Endpoint**: `http://localhost:8080/actuator/prometheus`
- **Metrics**: JVM, HTTP requests, database connections, Kafka consumers, Redis cache

### Health Checks

- **Endpoint**: `http://localhost:8080/actuator/health`

### Logs (Loki)

Application logs are automatically sent to Loki for centralized log aggregation.

### Distributed Tracing (Tempo)

- **Endpoint**: `http://localhost:4318`
- **Traces**: Request flows across services with timing information
- **Sampling**: 100% of requests traced (configurable)

### Grafana Dashboards

Access Grafana at `http://localhost:3000` (admin/admin)

Recommended dashboard: Import ID **11378** (JVM Micrometer)

## License

This project is part of the Skydive Forecast system.

## Contact

For questions or support, please contact me.
