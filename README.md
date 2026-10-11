# Task Management API

A REST API for managing tasks, built with Java and Spring Boot. This project demonstrates a layered backend structure, persistence with Spring Data JPA, request validation, centralized exception handling, and automated tests.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [API Endpoints](#api-endpoints)
- [Request and Response Examples](#request-and-response-examples)
- [Validation and Error Handling](#validation-and-error-handling)
- [Database](#database)
- [Testing](#testing)
- [Getting Started](#getting-started)
- [Configuration](#configuration)
- [Example Usage](#example-usage)
- [Project Structure](#project-structure)
- [Future Improvements](#future-improvements)

## Overview

Task Management API provides HTTP endpoints for creating, retrieving, updating, and deleting tasks. Each task has an identifier, a title, and a description.

The application uses a layered design: the controller handles HTTP requests, the service contains application logic and maps entities to response DTOs, and the repository provides persistence operations through Spring Data JPA.

## Features

The following features are implemented in the current source code:

- Retrieve all tasks.
- Retrieve a task by ID.
- Create a task.
- Fully update a task using `PUT`.
- Partially update a task using `PATCH`.
- Delete a task.
- Validate request DTOs with Jakarta Bean Validation.
- Handle task-not-found and custom invalid-attribute exceptions centrally.
- Return structured JSON responses for handled errors.

Validation currently requires a non-blank title with a maximum length of 255 characters for create and full-update requests. The PATCH request limits the title to 255 characters but does not apply the same non-blank constraint. The description has no Bean Validation constraints in the current DTOs.

## Tech Stack

| Technology | Role in this project |
|---|---|
| Java 21 | Application language; the Maven build sets `java.version` to `21`. |
| Spring Boot 4.1.1 | Application bootstrap and framework configuration, as declared in `pom.xml`. |
| Spring Web MVC | REST controllers and HTTP request/response handling. |
| Spring Data JPA | Repository abstraction and CRUD persistence operations. |
| Hibernate | JPA provider used by the Spring Data JPA stack. |
| PostgreSQL JDBC Driver | Runtime driver for connecting to PostgreSQL. |
| Jakarta Bean Validation | Request validation through constraints such as `@NotBlank` and `@Size`, triggered by `@Valid`. |
| JUnit Jupiter (JUnit 5) | Test framework provided through the Spring Boot test dependencies. |
| Mockito | Mocking and interaction verification in service and controller tests. |
| Spring MockMvc | Simulates MVC requests and checks HTTP responses without starting a full HTTP server for the MVC slice tests. |
| Maven | Dependency management and project build. |
| Git | Version control. |

## Architecture

The application follows a layered structure:

1. **Controller — `TaskController`**  
   Exposes the `/tasks` HTTP endpoints, accepts request DTOs, invokes the service, and selects HTTP response statuses where explicitly configured.

2. **Service — `TaskService`**  
   Contains task operations, retrieves and changes entities through the repository, throws `TaskNotFoundException` when a requested task does not exist, and maps `Task` entities to `TaskResponseDto` objects. DTO-to-entity field assignment for creation and updates is also performed in this layer.

3. **Repository — `TaskRepository`**  
   Extends `JpaRepository<Task, Long>` to use Spring Data JPA persistence operations.

4. **Entity — `Task`**  
   Represents the persisted task and maps to the database table named `task`. Its `id` is generated using `GenerationType.IDENTITY`.

5. **DTOs — request, response, and error objects**  
   `TaskRequestDto` is used for creation and full updates, `TaskPatchRequestDto` for partial updates, `TaskResponseDto` for task responses, and `ErrorResponseDto` for handled application errors.

6. **Global exception handler — `GlobalExceptionHandler`**  
   Uses `@RestControllerAdvice` to convert selected exceptions and request validation failures into HTTP error responses.

Typical request flow:

`HTTP request → TaskController → TaskService → TaskRepository → PostgreSQL`

The response travels back through the service and controller. The service maps entities to response DTOs; entities are not returned directly by the controller.

## API Endpoints

All endpoints use the `/tasks` base path. Status codes below are based on the controller and exception-handler implementation.

| Method | Endpoint | Purpose | Responses confirmed by the code |
|---|---|---|---|
| `GET` | `/tasks` | Retrieve all tasks | `200 OK` |
| `GET` | `/tasks/{id}` | Retrieve one task by ID | `200 OK`, `404 Not Found` when the task does not exist |
| `POST` | `/tasks` | Create a task | `201 Created`, `400 Bad Request` for request validation failure or the handled custom invalid-attribute exception |
| `PUT` | `/tasks/{id}` | Replace the task's title and description | `200 OK`, `400 Bad Request` for request validation failure, `404 Not Found` when the task does not exist |
| `PATCH` | `/tasks/{id}` | Update supplied non-null fields | `200 OK`, `400 Bad Request` for request validation failure, `404 Not Found` when the task does not exist |
| `DELETE` | `/tasks/{id}` | Delete a task | `204 No Content`, `404 Not Found` when the task does not exist |

The API does not currently define a separate `Location` header for successful creation. The `POST` response body contains the created task DTO.

## Request and Response Examples

Examples use `http://localhost:8080` as the local base URL. They assume the application is running and PostgreSQL is configured.

### Create a task

**Request — `POST /tasks`**

```json
{
  "title": "Prepare Java interview",
  "description": "Review Spring Boot and REST API fundamentals"
}
```

**Successful response — `201 Created`**

```json
{
  "id": 1,
  "title": "Prepare Java interview",
  "description": "Review Spring Boot and REST API fundamentals"
}
```

The ID is generated by the database; `1` is an illustrative example.

### Retrieve a task

**Request — `GET /tasks/1`**

**Successful response — `200 OK`**

```json
{
  "id": 1,
  "title": "Prepare Java interview",
  "description": "Review Spring Boot and REST API fundamentals"
}
```

### Fully update a task

**Request — `PUT /tasks/1`**

Both request fields are supplied for a full update.

```json
{
  "title": "Prepare for a Java interview",
  "description": "Practice Java, Spring, JPA, and testing"
}
```

**Successful response — `200 OK`**

```json
{
  "id": 1,
  "title": "Prepare for a Java interview",
  "description": "Practice Java, Spring, JPA, and testing"
}
```

### Partially update a task

**Request — `PATCH /tasks/1`**

Only the title is supplied. Fields omitted from the request are left unchanged by the service.

```json
{
  "title": "Review Spring MVC"
}
```

**Successful response — `200 OK`**

```json
{
  "id": 1,
  "title": "Review Spring MVC",
  "description": "Practice Java, Spring, JPA, and testing"
}
```

In the current implementation, a JSON `null` or an omitted field is treated as “do not update” by the service. The DTO does not distinguish those two cases.

### Task not found

For `GET /tasks/999`, the handled error response has this structure (`timestamp` is generated at runtime):

```json
{
  "timestamp": "2026-01-01T12:00:00",
  "status": 404,
  "error": "Task Not Found",
  "message": "Task with id 999 not found",
  "errors": null
}
```

### Request validation error

For example, `POST /tasks` with a blank title returns `400 Bad Request`. The validation handler currently returns a map with `timestamp`, `status`, `error`, and a field-level `errors` object:

```json
{
  "timestamp": "2026-01-01T12:00:00",
  "status": 400,
  "error": "Validation Error",
  "errors": {
    "title": "must not be blank"
  }
}
```

The timestamp and exact validation message are illustrative; the actual timestamp is generated at runtime and the message comes from the validation provider.

## Validation and Error Handling

- `TaskRequestDto` applies `@NotBlank` and `@Size(max = 255)` to `title`.
- `TaskPatchRequestDto` applies `@Size(max = 255)` to `title`; it does not currently use `@NotBlank`.
- The controller uses `@Valid` on create, PUT, and PATCH request bodies to trigger Bean Validation.
- `GlobalExceptionHandler`, annotated with `@RestControllerAdvice`, handles `TaskNotFoundException` as `404 Not Found` and `NotValidAttributeTaskException` as `400 Bad Request`.
- The handler overrides `handleMethodArgumentNotValid` to return `400 Bad Request` with field-level validation messages.
- `ErrorResponseDto` defines `timestamp`, `status`, `error`, `message`, and `errors`. The custom-exception handlers use this DTO. The validation handler currently builds a map directly, so its response shape is not identical to `ErrorResponseDto`.

## Database

The application uses PostgreSQL through the PostgreSQL JDBC driver. `Task` is a JPA entity mapped to the `task` table, and `TaskRepository` extends `JpaRepository` to provide persistence methods. Hibernate is used as the JPA provider.

The current `application.properties` contains:

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

`ddl-auto=update` is configured, so Hibernate is instructed to update the schema based on entity mappings at startup. No Flyway or Liquibase migration configuration is present in the supplied project files. SQL logging and formatted SQL output are enabled in the current configuration.

## Testing

The repository contains the following test classes:

- **`TaskServiceTest`** — 15 test methods covering retrieval, empty results, DTO mapping, creation, deletion, missing tasks, full updates, and PATCH behavior. It uses JUnit Jupiter and Mockito (`@ExtendWith(MockitoExtension.class)`, mocks, argument captors, and interaction verification).
- **`TaskControllerTest`** — 6 MVC test methods covering retrieval, a missing task, successful creation, a custom bad-request path, and a blank-title validation failure. It uses `@WebMvcTest`, `MockMvc`, and a Mockito bean override.
- **`TaskManagementApiApplicationTests`** — a Spring Boot context-load test.

These files confirm that the tests are present, but their presence alone does not confirm that the full test suite passes. The supplied `TaskServiceTest` contains a PATCH test whose expected title appears inconsistent with its setup: it sends only a description update but asserts `"New title"`. Run the test suite and correct that assertion before reporting a successful test run. Controller tests for PUT, PATCH, and DELETE are not currently present.

Run the tests from the project root:

```bash
mvn test
```

The project includes Maven wrapper scripts, but the supplied archive does not contain the `.mvn/wrapper` configuration files. Use an installed Maven version compatible with the project unless the wrapper configuration is restored.

## Getting Started

### Prerequisites

- JDK 21
- Maven installed locally (see the wrapper note in [Testing](#testing))
- A running PostgreSQL instance
- A PostgreSQL database and user with permission to connect to that database

### 1. Clone the repository

Replace the placeholder URL with the actual URL of your GitHub repository:

```bash
git clone <YOUR_REPOSITORY_URL>
cd task-management-api
```

### 2. Create a PostgreSQL database

Create a database and a user for the application using your local PostgreSQL administration tool or SQL client. The exact commands depend on your local PostgreSQL setup.

### 3. Configure the database connection

Update `src/main/resources/application.properties` with your local database URL, username, and password. Do not commit real credentials. See [Configuration](#configuration) for the required properties.

### 4. Run the application

```bash
mvn spring-boot:run
```

By default, Spring Boot applications use port `8080` unless configured otherwise. After startup, try `GET http://localhost:8080/tasks`.

### 5. Run tests

```bash
mvn test
```

Tests that load the full Spring application context may require a reachable database because the application is configured with a PostgreSQL datasource.

## Configuration

The supplied `src/main/resources/application.properties` declares these datasource properties, but their values are placeholders in the provided archive:

```properties
spring.application.name=task-management-api

spring.datasource.url=jdbc:postgresql://localhost:5432/task_management
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

This is an example configuration for local development, not a claim that environment-variable substitution is already set up elsewhere in the project. Set `DB_USERNAME` and `DB_PASSWORD` in your shell or IDE run configuration before starting the app, or replace the placeholders locally with your own credentials. Keep secrets out of version control.

The datasource URL follows the PostgreSQL JDBC format:

`jdbc:postgresql://<host>:<port>/<database>`

## Example Usage

The following examples assume the server is available at `http://localhost:8080`.

Create a task:

```bash
curl -i -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Read Effective Java","description":"Review selected chapters"}'
```

List all tasks:

```bash
curl -i http://localhost:8080/tasks
```

Get a task by ID:

```bash
curl -i http://localhost:8080/tasks/1
```

Fully update a task:

```bash
curl -i -X PUT http://localhost:8080/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Study Spring","description":"Review MVC and JPA"}'
```

Partially update a task:

```bash
curl -i -X PATCH http://localhost:8080/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"description":"Practice MockMvc tests"}'
```

Delete a task:

```bash
curl -i -X DELETE http://localhost:8080/tasks/1
```

A successful delete returns `204 No Content` with no response body.

## Project Structure

The following structure is based on the supplied archive. Generated files, Git internals, and IDE metadata are omitted.

```text
task-management-api/
├── pom.xml
├── mvnw
├── mvnw.cmd
├── src/
│   ├── main/
│   │   ├── java/com/example/task_management_api/
│   │   │   ├── TaskManagementApiApplication.java
│   │   │   ├── controller/
│   │   │   │   └── TaskController.java
│   │   │   ├── dto/
│   │   │   │   ├── ErrorResponseDto.java
│   │   │   │   ├── TaskPatchRequestDto.java
│   │   │   │   ├── TaskRequestDto.java
│   │   │   │   └── TaskResponseDto.java
│   │   │   ├── entity/
│   │   │   │   └── Task.java
│   │   │   ├── exception/
│   │   │   │   ├── GlobalExceptionHandler.java
│   │   │   │   ├── NotValidAttributeTaskException.java
│   │   │   │   └── TaskNotFoundException.java
│   │   │   ├── repository/
│   │   │   │   └── TaskRepository.java
│   │   │   └── service/
│   │   │       └── TaskService.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/example/task_management_api/
│           ├── TaskManagementApiApplicationTests.java
│           └── service/
│               ├── TaskControllerTest.java
│               └── TaskServiceTest.java
└── README.md
```

## Future Improvements

The following are potential next steps, not implemented features:

- Add MVC tests for PUT, PATCH, and DELETE, including missing-task cases and response-body assertions.
- Fix the inconsistent PATCH test assertion and run the complete test suite in a configured environment.
- Return a consistent error schema for Bean Validation failures and custom exceptions, ideally using `ErrorResponseDto` consistently.
- Add validation tests for title length and PATCH edge cases; decide explicitly whether an empty PATCH request should be accepted.
- Move local database credentials to environment variables or an external local configuration file and document the setup consistently.
- Consider database migrations with Flyway or Liquibase if schema changes need to be versioned and reproducible.
- Add pagination and sorting to the list endpoint if the API needs to support larger task collections.
- Add API documentation (for example, OpenAPI) once the endpoint contract is stable.
- Add CI checks to compile the project and run tests on each change.

