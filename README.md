# Secret Stash

Secret Stash is a secure note-taking service built with Spring Boot and Kotlin. It allows users to create, retrieve,
update, and delete personal notes with proper authentication and authorization using Keycloak.

## Features

- Secure note management with OAuth2 authentication
- Create, read, update, and delete notes
- Set optional expiration time for notes (TTL)
- View history of note changes
- Rate limiting protection
- Comprehensive API documentation
- Docker Compose setup for easy local deployment

## Technology Stack

- **Backend**: Kotlin + Spring Boot 3
- **Database**: PostgreSQL 17
- **Cache**: Redis 7
- **Authentication**: Keycloak 26
- **Documentation**: OpenAPI (Swagger)
- **Build Tool**: Gradle with Kotlin DSL
- **Test Containers**: For integration testing

## Prerequisites

- JDK 21 or higher
- Docker and Docker Compose
- Gradle 8.x (or use the included Gradle wrapper)

## Getting Started

### Running with Docker Compose (Demo Mode)

The easiest way to run the application is using the provided Docker Compose setup:

1. Clone the repository
2. Run the demo script:
   ```bash
   ./demo/run_demo.sh
   ```

This approach doesn't require building the project locally as it pulls the latest image from the Docker registry. You
only need to have Docker installed.

This will start:

- The Secret Stash application on port 8081
- PostgreSQL databases (for the app and Keycloak)
- Redis cache
- Keycloak authentication server on port 8080

### Running Locally

To run the application locally:

1. Clone the repository
2. Build and run the application:
   ```bash
   ./gradlew bootRun
   ```

Spring Boot will automatically start the required PostgreSQL and Redis instances using the Docker Compose integration (
via the compose.yaml file in the project root).

## Authentication

The application uses Keycloak for authentication. In the demo environment, two users are pre-configured:

- **User 1**:
    - Username: `user1`
    - Password: `user1@Test`

- **User 2**:
    - Username: `user2`
    - Password: `user2@Test`

The Keycloak login and account management page is available at:
http://localhost:8080/realms/bitnet/account

Additional test users can be registered through this page.

For API access, the OAuth2 client credentials are:

- Client ID: `secret-stash`
- Client Secret: `secret-stash`

You can obtain an access token using curl:

```bash
curl -X POST http://localhost:8080/realms/bitnet/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=secret-stash" \
  -d "client_secret=secret-stash" \
  -d "username=user1" \
  -d "password=user1@Test"
```

The Swagger UI also supports authentication using these credentials.

## API Documentation

The API is documented using OpenAPI (Swagger). After starting the application, the documentation is available at:

http://localhost:8081/docs/swagger-ui.html

## Note Management

The application provides endpoints for:

- Creating notes with optional TTL (time-to-live)
- Retrieving notes
- Updating notes
- Deleting notes
- Viewing note history

Full API details are available in the Swagger documentation.

## Project Structure

The project is organized into the following packages:

- `config` - Spring Boot related configurations (security, web, exceptions, etc.)
- `note` - Main business domain for notes management
    - `api` - REST controllers and API interfaces
    - `domain` - Domain models and entities
    - `dto` - Data Transfer Objects
    - `exception` - Domain-specific exceptions
    - `infrastructure` - Data access repositories
    - `job` - Scheduled jobs (e.g., note expiration)
    - `service` - Business logic and service implementations
- `util` - Utility services and components
    - `auth` - Authentication and token handling services
    - `ratelimiter` - Rate limiting implementation

## Development

### Build

```bash
./gradlew build
```

### Test

```bash
./gradlew test
```

### Code Quality

The project uses:

- ktlint for code style enforcement
- JaCoCo for code coverage

Run code style checks:

```bash
./gradlew ktlintCheck
```

Generate coverage report:

```bash
./gradlew jacocoTestReport
```

## License

This project is licensed under the MIT License.
