# Skydive Forecast - Gateway

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
- **Spring Cloud Gateway**: Server WebMVC
- **SpringDoc OpenAPI**: 2.8.13
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

### Environment-Specific Configuration

All profiles use the same microservice routing configuration, connecting to local instances:

- Users Service: `http://localhost:8081`
- Analyses Service: `http://localhost:8082`
- Locations Service: `http://localhost:8083`

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
| `/api/users/**` | Users Service | 8081 |
| `/api/analyses/**` | Analyses Service | 8082 |
| `/api/locations/**` | Locations Service | 8083 |

**Note**: The gateway strips the first 2 path segments (`/api/{service}`) before forwarding to microservices.

### API Documentation

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Docs**: `http://localhost:8080/v3/api-docs`

The Swagger UI aggregates documentation from all microservices:
- `/v3/api-docs/users` - Users Service API
- `/v3/api-docs/analyses` - Analyses Service API
- `/v3/api-docs/locations` - Locations Service API

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
│   │   │   └── infrastructure/config/
│   │   │       └── OpenApiConfig.java
│   │   └── resources/
│   │       ├── application.yaml
│   │       ├── application-dev.yaml
│   │       ├── application-test.yaml
│   │       └── application-prod.yaml
│   └── test/
├── pom.xml
└── README.md
```

## Development

### Building

```bash
mvn clean package
```

### Running the JAR

After building, you can run the application using:

```bash
java -jar target/skydive-forecast-gateway-1.0.0-SNAPSHOT.jar
```

## License

This project is part of the Skydive Forecast system.

## Contact

For questions or support, please contact me.
