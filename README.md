# Zoo Application API - Full Setup, Documentation, Endpoints, and Tests

# Overview
Zoo Application is a modern, RESTful API for managing a Zoo, built with Spring Boot and MongoDB. It includes full OpenAPI documentation.

# Table of Contents
Table of Contents:
- Overview
- Features
- Architecture & Design
- Entity Diagram
- Technology Stack
- Getting Started
- Running Tests
- API Documentation
- Endpoints
- Error Handling
- Pagination & Sorting
- License

# Features
echo "Features:
- Animals Management: CRUD, placement in rooms, favorite rooms
- Rooms Management: CRUD, favorite room stats
- Validation: @Valid, @NotNull, @NotBlank
- Error Handling: Standardized API errors
- Integration Testing: Spring Boot + Testcontainers
- OpenAPI Documentation: Swagger support for easy API exploration

# Architecture & Design
Architecture & Design:
- Controller Layer (REST API): Receives HTTP requests and maps them to service calls.
- Service Layer (Business Logic): Implements business logic and validation.
- Repository Layer (MongoDB): Interfaces with MongoDB.

# Entity Diagram
Entity Diagram:
Animal: id, title, created, updated, located, currentRoomId, favouriteRoomIds
Room: id, title, created, updated
Relationships:
- Animal can have a current room (currentRoomId)
- Animal can have multiple favorite rooms (favouriteRoomIds)
- Room can have multiple animals assigned or marked as favorite

# Technology Stack
Technology Stack:
- Language: Java 17
- Framework: Spring Boot
- Database: MongoDB (dev, test, prod), Testcontainers MongoDB (integration tests)
- API Docs: OpenAPI / Swagger
- Validation: Jakarta Bean Validation (@NotNull, @NotBlank)
- Testing: JUnit 5, MockMvc, Testcontainers
- JSON Mapper: Jackson
- Logging: SLF4J (LoggerFactory), Logback

# Getting Started
Getting Started:

# Clone repository
git clone https://github.com/abbaspayami/zoo_app.git

# Build and run
./mvnw clean install
./mvnw spring-boot:run

# Access API documentation
Access API documentation:
Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI spec: http://localhost:8080/v3/api-docs

# Running Tests
Running Tests (Testcontainers MongoDB, no local DB required):
./mvnw test

# Endpoints

Endpoints:

## Animals
POST /animals – Create an animal

GET /animals/{id} – Get animal by ID

PUT /animals/{id} – Update animal

DELETE /animals/{id} – Delete animal

POST /animals/{id}/place – Place animal in a room

PUT /animals/{id}/move – Move animal to another room

DELETE /animals/{id}/room – Remove animal from current room

POST /animals/{id}/favourites – Add a favorite room

DELETE /animals/{id}/favourites/{roomId} – Remove a favorite room

GET /animals/room/{roomId} – List animals in a room (pagination)

## Rooms
POST /rooms – Create a room

GET /rooms/{id} – Get room by ID

PUT /rooms/{id} – Update room

DELETE /rooms/{id} – Delete room

GET /rooms/favourites/stats – List favourite room statistics

# Error Handling
Error Handling 

Example:
{
  \"status\": 404,

  \"error\": \"Not Found\",

  \"message\": \"Animal not found with id = 123\",

  \"timestamp\": \"2025-11-23T10:12:45.123Z\

}

status: HTTP status code

error: HTTP status reason phrase

message: Detailed error message

timestamp: Time of error

# Pagination & Sorting
Pagination & Sorting Example:

GET /animals/room/{roomId}?sortBy=title&order=asc&page=0&size=10

Response:
{
  \"items\": [...],

  \"page\": 0,

  \"size\": 10,

  \"totalElements\": 42,

  \"totalPages\": 5

}

Uses DTOs for input/output

All input fields are validated with Bean Validation

Tests use Testcontainers for integration"

