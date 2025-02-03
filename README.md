# Spring Boot Demo Application

This is a simple Spring Boot application that exposes a `/start-test` endpoint.

## Requirements
- Java 17 or higher
- Maven 3.6 or higher

## Running the Application
1. Build the project:
```bash
mvn clean install
```

2. Run the application:
```bash
mvn spring-boot:run
```

3. Access the endpoint:
- Open your browser or use curl: `http://localhost:8080/start-test`

## Endpoints
- GET `/start-test`: Returns a test message
