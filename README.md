# Car REST Service

### Java 21 · Spring Boot 4 · REST API · OAuth2 / JWT · PostgreSQL · Docker Compose

![Java 21](https://img.shields.io/badge/Java-21-E76F00?logo=openjdk&logoColor=white)
![Spring Boot 4](https://img.shields.io/badge/Spring%20Boot-4-6DB33F?logo=springboot&logoColor=white)
![Spring Web MVC](https://img.shields.io/badge/Web-Spring%20MVC-6DB33F?logo=spring&logoColor=white)
![Spring Security](https://img.shields.io/badge/Security-Spring%20Security-6DB33F?logo=springsecurity&logoColor=white)
![PostgreSQL 16](https://img.shields.io/badge/Database-PostgreSQL%2016-4169E1?logo=postgresql&logoColor=white)
![Keycloak](https://img.shields.io/badge/Identity-Keycloak-4D4D4D?logo=keycloak&logoColor=white)
![Docker](https://img.shields.io/badge/Containers-Docker-2496ED?logo=docker&logoColor=white)
![JUnit 5](https://img.shields.io/badge/Testing-JUnit%205-25A162?logo=junit5&logoColor=white)
![Testcontainers](https://img.shields.io/badge/Integration-Testcontainers-2496ED?logo=docker&logoColor=white)

A RESTful backend service for managing a car catalog, built with **Java 21**, **Spring Boot 4**, **Spring Data JPA**, and **PostgreSQL**.

The application provides CRUD operations for cars, manufacturers, models, and categories, along with filtering, pagination, input validation, and relational data management.

Authentication and authorization are implemented using **Spring Security**, **Keycloak**, and **OAuth2/JWT**. Public read endpoints and scope-protected write operations are documented through OpenAPI and verified with automated integration tests.

The project supports both local development and fully containerized execution with **Docker Compose**, including PostgreSQL and Keycloak.

---

<details open>
<summary><h2>Features</h2></summary>

- **REST API** — CRUD operations for cars, manufacturers, models, and categories.
- **Search and Pagination** — filtering by car attributes and paginated responses.
- **Validation** — request validation, business rules, and structured error handling.
- **Database Persistence** — Spring Data JPA, Hibernate, and PostgreSQL.
- **Database Migrations** — schema creation and CSV catalog import using Flyway.
- **API Security** — JWT validation and scope-based authorization using Keycloak.
- **API Documentation** — OpenAPI 3 and Swagger UI with OAuth2 support.
- **Integration Testing** — automated tests with JUnit 5, Spring Boot Test, and Testcontainers.
- **Postman Collection** — API integration tests, including authentication and authorization scenarios.
- **Containerization** — multi-stage Docker build and separate development/deployment configurations.

</details>

---

<details open>
<summary><h2>Technology Stack</h2></summary>

| Area | Technologies |
|---|---|
| Language & Framework | Java 21, Spring Boot 4, Spring Web MVC |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL 16 |
| Database Migrations | Flyway |
| Security | Spring Security, OAuth2 Resource Server, JWT, Keycloak |
| API Documentation | Springdoc OpenAPI 3, Swagger UI |
| Testing | JUnit 5, Spring Boot Test, Mockito, Testcontainers |
| API Testing | Postman |
| Containers | Docker, Docker Compose |
| Build | Maven |

</details>

---

<details open>
<summary><h2>Environment Configuration</h2></summary>

Create a `.env` file in the project root.

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

> [!IMPORTANT]
> The `.env` file is excluded from Git and must not be committed. Use local development credentials only.

</details>

---

<details open>
<summary><h2>Running the Application</h2></summary>

### Deployment Modes

The project provides two Docker Compose configurations:

| Configuration | Application | PostgreSQL | Keycloak |
|---|---|---|---|
| [`compose.yaml`](compose.yaml) | Docker | Docker | Docker |
| [`compose.local.yaml`](compose.local.yaml) | Local JVM / IDE | Docker | Docker |

The local configuration uses Docker Compose `extends` to reuse PostgreSQL and Keycloak settings from the main configuration.

Both configurations share the same external Docker volumes.

### Prerequisites

- Docker Desktop and Docker Compose
- Java 21 for local development
- Maven Wrapper (included in the repository)

### Initial Setup

Clone the repository:

```bash
git clone https://github.com/Yurii-Kor/car-rest-service.git
cd car-rest-service
```

Create the `.env` file as described above.

Create the external Docker volumes:

```bash
docker volume create car-rest-postgres-data
docker volume create car-rest-keycloak-data
```

### Local Development

Run `CarRestServiceApplication` from an IDE or start Spring Boot with Maven Wrapper.

On Windows PowerShell:

```powershell
.\mvnw.cmd spring-boot:run
```

Spring Boot automatically starts PostgreSQL and Keycloak using `compose.local.yaml`.

Alternatively, start the infrastructure manually:

```bash
docker compose -f compose.local.yaml up -d
```

In local mode:

- Spring Boot runs on the host at `localhost:8080`.
- PostgreSQL is accessible at `localhost:5433`.
- Keycloak is accessible at `localhost:8180`.

### Full Docker Deployment

Stop the local application and its Compose stack before switching modes.

Build and start the complete stack:

```bash
docker compose up -d --build
```

Stop the stack:

```bash
docker compose down
```

External volumes preserve PostgreSQL and Keycloak data between restarts.

### Service Addresses

| Service | Host Address |
|---|---|
| REST API | `http://localhost:8080` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| Keycloak | `http://localhost:8180` |
| PostgreSQL (local mode only) | `localhost:5433` |

In full Docker mode, Spring Boot connects to PostgreSQL through `postgres:5432` and retrieves Keycloak signing keys through `keycloak:8080`.

PostgreSQL is not exposed to the host in full Docker mode.

</details>

---

<details>
<summary><h2>Docker Architecture</h2></summary>

### Multi-Stage Docker Build

The [`Dockerfile`](Dockerfile) uses a multi-stage build to separate application compilation from execution.

- **Build stage** — JDK 21, Maven, and BuildKit dependency caching.
- **Layer extraction** — Spring Boot layered JAR separates dependencies, the Boot loader, and application code.
- **Runtime stage** — JRE 21 with a non-root application user.
- **Version-independent packaging** — Dockerfile wildcard matching supports changes to the Maven artifact version.

### Resource Limits

| Service | Memory Limit |
|---|---:|
| Spring Boot (full mode) | 1 GiB |
| PostgreSQL (both modes) | 512 MiB |
| Keycloak (both modes) | 2 GiB |

The Spring Boot JVM uses `-XX:MaxRAMPercentage=50.0` in full Docker mode.

CPU limits are not configured.

Application logs are written to stdout/stderr and managed by Docker with log rotation.

### Persistence

Two external Docker volumes are used:

- `car-rest-postgres-data`
- `car-rest-keycloak-data`

These preserve database and identity-provider data across Compose stack recreation.

</details>

---

<details open>
<summary><h2>API Security</h2></summary>

The application uses **Keycloak** as its OAuth2 authorization server and **Spring Security** as a JWT-validating resource server.

Authorization is based on token scopes rather than Keycloak-specific roles.

### Access Rules

| Operation | Required Permission |
|---|---|
| GET endpoints | Public |
| Car write operations | `cars:write` |
| Manufacturer write operations | `catalog:write` |
| Model write operations | `catalog:write` |
| Category write operations | `catalog:write` |

- Missing authentication returns HTTP `401 Unauthorized`.
- Insufficient permissions return HTTP `403 Forbidden`.

### Keycloak Configuration

Realm configuration:

[`docker/keycloak/import/car-service-realm.json`](docker/keycloak/import/car-service-realm.json)

Both Compose configurations import the `car-service` realm during first initialization. Existing realms are preserved.

### Development Users

| User | Granted Scopes |
|---|---|
| `car-editor` | `cars:write` |
| `catalog-editor` | `catalog:write` |
| `car-tester` | `cars:write`, `catalog:write` |

Test user passwords are configured through environment variables.

> [!NOTE]
> Keycloak is started in development mode. The provided configuration is intended for local development and integration testing, not production deployment.

</details>

---

<details open>
<summary><h2>API Documentation</h2></summary>

The API is documented using **OpenAPI 3** and **Springdoc**.

Documentation is generated from the application and its OpenAPI annotations.

### Local Documentation

- **Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

Swagger UI supports OAuth2 Authorization Code with PKCE and JWT Bearer authentication.

</details>

---

<details>
<summary><h2>Postman Integration Tests</h2></summary>

The repository includes a Postman collection for API integration testing.

**Collection:**

[`docs/car-rest-service.integration-tests.postman_collection.json`](docs/car-rest-service.integration-tests.postman_collection.json)

The collection covers:

- Public endpoints and read operations
- CRUD operations
- Request validation
- Authentication and authorization
- Dependent requests using generated entity IDs

### Postman Environment

Create a Postman environment named `Car REST - Local`:

| Variable | Value |
|---|---|
| `baseUrl` | `http://localhost:8080` |
| `keycloakUrl` | `http://localhost:8180` |
| `realm` | `car-service` |
| `clientId` | `car-rest-postman` |
| `callbackUrl` | `https://oauth.pstmn.io/v1/callback` |

### Authentication

The collection uses **OAuth2 Authorization Code with PKCE** and the public `car-rest-postman` client.

1. Import the collection and select the environment.
2. Open the collection's **Authorization** tab.
3. Select **Get New Access Token**.
4. Authenticate as `car-tester`.
5. Select **Use Token**.
6. Run the collection using Postman Collection Runner.

Protected requests inherit OAuth2 authentication from the collection.

The **Public Access** folder uses `No Auth`.

Collection variables manage dependent requests and generated entity IDs. No passwords or JWT tokens are hardcoded.

</details>

---

<details open>
<summary><h2>Automated Testing</h2></summary>

The project includes automated tests covering application logic, persistence, REST controllers, security, and integration scenarios.

### Testing Tools

- **JUnit 5** — automated test execution.
- **Mockito** — mocking dependencies in isolated tests.
- **Spring Boot Test** — application context and integration testing.
- **Testcontainers** — integration environments with PostgreSQL and Keycloak.

### Running Tests

Ensure Docker Desktop is available.

On Windows PowerShell:

```powershell
.\mvnw.cmd test
```

The suite includes unit, integration, persistence, and security tests.

Docker image builds skip test execution; tests are run separately.

</details>

---

## Implementation Notes

- Flyway manages database schema creation and migration.
- Hibernate uses `ddl-auto=validate` to validate the database mapping without modifying the schema.
- Database sample data is imported through Flyway migrations.
- Authorization checks use OAuth2 scopes instead of application-specific role names.
- PostgreSQL and Keycloak share external volumes between development and full Docker configurations.
- Docker Compose uses HTTP and Keycloak development mode for local development and integration testing.
- Production deployment requires additional security and operational configuration.
