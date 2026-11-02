# Car REST Service

A RESTful API for managing a car catalog, built with Java 21 and Spring Boot 4.

## Tech Stack

- Java 21, Spring Boot 4, Spring Web MVC
- Spring Data JPA, Hibernate, PostgreSQL 16
- Spring Security, OAuth2 Resource Server, Keycloak
- Springdoc OpenAPI 3, Swagger UI
- Flyway, Maven
- Docker, Docker Compose
- JUnit 5, Testcontainers

## Features

- CRUD operations for cars, manufacturers, models, and categories.
- Car filtering, pagination, and validation.
- Database migrations and CSV data import using Flyway.
- OAuth2/JWT authentication with scope-based authorization.
- OpenAPI documentation and Postman integration tests.
- Automated tests and containerized deployment.

## Environment Configuration

Create a `.env` file in the project root:

| Variable | Example / Description |
|---|---|
| `POSTGRES_DB` | `car_rest_db` |
| `POSTGRES_USER` | `car_rest` |
| `POSTGRES_PASSWORD` | Database password |
| `KEYCLOAK_ADMIN_USERNAME` | `admin` |
| `KEYCLOAK_ADMIN_PASSWORD` | Keycloak administrator password |
| `KEYCLOAK_PUBLIC_URL` | `http://localhost:8180` |
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8180/realms/car-service` |
| `CAR_EDITOR_PASSWORD` | Car editor test user password |
| `CATALOG_EDITOR_PASSWORD` | Catalog editor test user password |
| `CAR_TESTER_PASSWORD` | Combined-permissions test user password |

All ten variables are required for Docker Compose.

The `.env` file is excluded from Git and must not be committed.

## Running the Application

The project supports two Docker Compose configurations:

- `compose.yaml` — complete stack with Spring Boot, PostgreSQL, and Keycloak.
- `compose.local.yaml` — PostgreSQL and Keycloak in Docker, Spring Boot running locally.

The local configuration uses Docker Compose `extends` to inherit PostgreSQL and Keycloak settings from `compose.yaml`, avoiding configuration duplication.

Both modes share the same external Docker volumes.

### Initial Setup

Docker Desktop and Docker Compose are required. Java 21 is additionally required for local development.

Create the external volumes once:

```powershell
docker volume create car-rest-postgres-data
docker volume create car-rest-keycloak-data
```

### Local Development

Run `CarRestServiceApplication` from Eclipse or use:

```powershell
.\mvnw.cmd spring-boot:run
```

Spring Boot automatically starts PostgreSQL and Keycloak using `compose.local.yaml`.

Alternatively, start the infrastructure manually:

```powershell
docker compose -f compose.local.yaml up -d
```

Spring Boot runs on the host and connects to PostgreSQL at `localhost:5433` and Keycloak at `localhost:8180`.

### Full Docker Deployment

Stop the local application and Compose stack before switching modes.

Start the complete stack:

```powershell
docker compose up -d --build
```

Stop the stack:

```powershell
docker compose down
```

External volumes preserve PostgreSQL and Keycloak data.

### Service Addresses

| Service | Host Address |
|---|---|
| REST API | `http://localhost:8080` |
| Keycloak | `http://localhost:8180` |
| PostgreSQL (local mode only) | `localhost:5433` |

In full Docker mode, Spring Boot connects to PostgreSQL at `postgres:5432` and retrieves Keycloak signing keys through `keycloak:8080`.

PostgreSQL is not exposed to the host in full Docker mode.

### Docker Configuration

The Dockerfile uses a multi-stage build with JDK 21, Maven BuildKit caching, and a JRE 21 runtime.

Spring Boot's layered JAR is extracted into separate Docker layers for dependencies, the Boot loader, and application code. The JAR filename includes the Maven project version, while Dockerfile wildcard matching supports version changes without hardcoded paths.

The application runs as a non-root user.

Docker Compose memory limits:

| Service | Memory Limit |
|---|---|
| Spring Boot (full mode) | 1 GiB |
| PostgreSQL (both modes) | 512 MiB |
| Keycloak (both modes) | 2 GiB |

Spring Boot uses `-XX:MaxRAMPercentage=50.0` in full Docker mode. CPU limits are not configured.

Application logs are written to stdout/stderr and managed by Docker with log rotation.

## API Security

The application uses Keycloak for OAuth2 authorization and Spring Security for JWT validation.

- GET endpoints are publicly accessible.
- Car write operations require `cars:write`.
- Manufacturer, model, and category write operations require `catalog:write`.
- Missing authentication returns `401`; insufficient permissions return `403`.

Authorization is based on scopes rather than Keycloak-specific roles.

### Keycloak Configuration

Realm configuration:

`docker/keycloak/import/car-service-realm.json`

Both Compose modes import the `car-service` realm on first initialization. Existing realms are preserved.

| User | Granted Scopes |
|---|---|
| `car-editor` | `cars:write` |
| `catalog-editor` | `catalog:write` |
| `car-tester` | `cars:write`, `catalog:write` |

Test user passwords are configured through environment variables.

## API Documentation

- **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`

Swagger UI supports OAuth2 Authorization Code with PKCE and JWT Bearer authentication.

## Postman Integration Tests

Collection:

`docs/car-rest-service.integration-tests.postman_collection.json`

The collection covers public endpoints, CRUD operations, validation, and authorization.

Configure a Postman environment named `Car REST - Local`:

| Variable | Value |
|---|---|
| `baseUrl` | `http://localhost:8080` |
| `keycloakUrl` | `http://localhost:8180` |
| `realm` | `car-service` |
| `clientId` | `car-rest-postman` |
| `callbackUrl` | `https://oauth.pstmn.io/v1/callback` |

The collection uses **OAuth2 Authorization Code with PKCE** and the public `car-rest-postman` client.

To run the tests:

1. Import the collection and select the environment.
2. In the collection's Authorization tab, obtain a token using **Get New Access Token**.
3. Authenticate as `car-tester` and select **Use Token**.
4. Run the collection using Postman Collection Runner.

Protected requests inherit OAuth2 authentication from the collection. The Public Access folder uses No Auth.

Collection variables manage dependent requests and generated entity IDs. No passwords or JWT tokens are hardcoded.

## Automated Tests

The project includes unit, integration, persistence, and security tests using JUnit 5, Spring Boot Test, and Testcontainers (PostgreSQL and Keycloak).

Run tests with Docker Desktop available:

```powershell
.\mvnw.cmd test
```

Docker image builds skip test execution; tests are run separately.

## Notes

- Flyway manages the database schema; Hibernate uses `ddl-auto=validate`.
- PostgreSQL and Keycloak data are stored in external Docker volumes.
- Docker Compose configurations use HTTP and Keycloak development mode and are intended for local development and integration testing.
