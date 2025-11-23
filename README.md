🦁 Zoo Management API
A modern, RESTful API for managing a Zoo, built with Spring Boot and MongoDB, with full OpenAPI documentation and integration tests using Testcontainers.

Table of Contents
Overview
Features
Architecture & Design
Entity Diagram
Technology Stack
Getting Started
Running Tests
API Documentation
Endpoints
Error Handling
Pagination & Sorting
License
Overview
This API provides endpoints to:

Create, read, update, and delete animals and rooms.
Assign animals to rooms.
Assign favorite rooms to animals.
Move animals between rooms.
List animals in a room with pagination and sorting.
Generate statistics of favorite rooms.
The system uses MongoDB for persistence, with embedded MongoDB in tests for fast integration testing. Testcontainers is used for containerized integration tests.

Features
Animals Management: CRUD, placement in rooms, favorite rooms
Rooms Management: CRUD, favorite room stats
Validation: @Valid, @NotNull, @NotBlank
Error Handling: Standardized API errors
Integration Testing: Spring Boot + Testcontainers
OpenAPI Documentation: Swagger support for easy API exploration
Architecture & Design
The application is layered:

Controller Layer (REST API)

Service Layer (Business Logic)

Repository Layer (MongoDB)

Controller Layer: Receives HTTP requests and maps them to service calls.

Service Layer: Implements business logic and validation.

Repository Layer: Interfaces with MongoDB.

Entity Diagram
Animal and Room (simplified)

Animal

id: String
title: String
located: Date
currentRoomId: String
favouriteRoomIds: Set<String>
Room

id: String
title: String
Relationships:

An Animal can have a current room (currentRoomId).
An Animal can have multiple favorite rooms (favouriteRoomIds).
A Room can have multiple animals assigned or marked as favorite.
Technology Stack
Layer: Backend
Language: Java 17
Framework: Spring Boot
Database: MongoDB (production), Testcontainers MongoDB (integration tests)
API Docs: OpenAPI / Swagger
Validation: Jakarta Bean Validation (@NotNull, @NotBlank)
Testing: JUnit 5, MockMvc, Testcontainers
JSON Mapper: Jackson
Logging: SLF4J / Logback
Getting Started
Clone the repository
git clone https://github.com/your-org/zoo-management-api.git
cd zoo-management-api
Configure MongoDB URI for tests (optional for default)
Use application.yml / application-test.yml or dynamic properties (if using Testcontainers)
Build and run
./mvnw clean install
./mvnw spring-boot:run
Access API documentation
Swagger UI: http://localhost:8080/swagger-ui.html
OpenAPI spec: http://localhost:8080/v3/api-docs
Running Tests
Tests use Testcontainers MongoDB (no local DB required).
Run all tests:
./mvnw test
OpenAPI Documentation
Auto-generated API docs via Springdoc/OpenAPI.
Access at:
/v3/api-docs
/swagger-ui.html
Endpoints
Animals

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
Rooms

POST /rooms – Create a room
GET /rooms/{id} – Get room by ID
PUT /rooms/{id} – Update room
DELETE /rooms/{id} – Delete room
GET /rooms/favourites/stats – List favourite room statistics
Error Handling
Standardized API error response:

{
"status": 404,
"error": "Not Found",
"message": "Animal not found with id = 123",
"timestamp": "2025-11-23T10:12:45.123Z"
}

status: HTTP status code
error: HTTP status reason phrase
message: Detailed error message
timestamp: Time of error
Pagination & Sorting
List animals in a room with pagination and sorting:

GET /animals/room/{roomId}?sortBy=title&order=asc&page=0&size=10

Response:
{
"items": [...],
"page": 0,
"size": 10,
"totalElements": 42,
"totalPages": 5
}

Uses DTOs for input/output
All input fields are validated with Bean Validation
Tests use