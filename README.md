
# Car REST Service

A RESTful web application for managing and querying car catalog data, built with Java and Spring Boot.

## Tech Stack

- Java 21 & Spring Boot 4
- Spring Web MVC
- Spring Data JPA / Hibernate
- Spring Security / OAuth2 Resource Server
- Keycloak
- Springdoc OpenAPI 3 / Swagger UI
- PostgreSQL 16
- Flyway Database Migrations
- Docker Compose
- Maven
- JUnit 5 / Testcontainers

## Project Status

**Under development**

Implemented functionality includes:

- REST API for cars, manufacturers, models, and categories.
- Database schema management and CSV data import using Flyway.
- Filtering, pagination, and CRUD operations.
- OAuth2 authorization with Keycloak and scope-based access control.
- OpenAPI 3 documentation and Postman integration tests.
- Automated integration and security tests.

## Environment Variables

The application requires the following environment variables, defined in a `.env` file in the project root.

| Variable | Description | Example |
|---|---|---|
| `POSTGRES_DB` | PostgreSQL database name | `car_rest_db` |
| `POSTGRES_USER` | PostgreSQL database username | `car_rest` |
| `POSTGRES_PASSWORD` | PostgreSQL database password | `<database-password>` |
| `KEYCLOAK_ADMIN_USERNAME` | Keycloak administrator username | `admin` |
| `KEYCLOAK_ADMIN_PASSWORD` | Keycloak administrator password | `<admin-password>` |
| `KEYCLOAK_ISSUER_URI` | Keycloak realm issuer URL used for JWT validation | `http://localhost:8180/realms/car-service` |
| `CAR_EDITOR_PASSWORD` | Password for the car editor test user | `<car-editor-password>` |
| `CATALOG_EDITOR_PASSWORD` | Password for the catalog editor test user | `<catalog-editor-password>` |
| `CAR_TESTER_PASSWORD` | Password for the test user with both write permissions | `<car-tester-password>` |

All nine variables are required for the local development setup.

Docker Compose reads the `.env` file to configure PostgreSQL and Keycloak. Spring Boot imports the same file through `spring.config.import` in `application.yaml`.

The test user passwords are injected into the Keycloak realm configuration during import. No passwords or access tokens are stored in the realm JSON.

The `.env` file is excluded from Git and must not be committed.

## Running Locally

1. Create a `.env` file in the project root and configure all required variables listed above.
2. Ensure Docker Desktop is running.
3. Start PostgreSQL and Keycloak:

   ```powershell
   docker compose up -d
   ```

4. Verify the containers:

   ```powershell
   docker compose ps
   ```

5. Start the Spring Boot application from Eclipse or run:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

The application connects to PostgreSQL at `localhost:5433` and uses Keycloak at `localhost:8180`.

### Keycloak Realm Import

The `car-service` Keycloak realm is automatically imported from:

`docker/keycloak/import/car-service-realm.json`

The Docker Compose configuration mounts this directory into the Keycloak container and starts Keycloak with the `--import-realm` option.

The import includes the required clients, roles, scopes, and test users. Test user passwords are provided through environment variables.

Keycloak imports the realm only if it does not already exist in its database. Changes to the JSON file are therefore not automatically applied to an existing realm.

### Docker Volumes

The Docker Compose configuration uses an external PostgreSQL volume named `car-rest-postgres-data`.

For a fresh installation, create it before starting the containers:

```powershell
docker volume create car-rest-postgres-data
```

## API Security

The application uses Keycloak as an OAuth2 authorization server and Spring Security as an OAuth2 resource server.

API access is controlled through OAuth2 scopes rather than application-specific roles.

- GET requests are publicly accessible without authentication.
- POST, PUT, PATCH, and DELETE requests require a valid JWT access token.
- Car write operations require the `cars:write` scope.
- Manufacturer, car model, and category write operations require the `catalog:write` scope.

The API validates incoming JWT access tokens and checks the required scopes before allowing protected operations.

Authorization rules are centralized in Spring Security configuration instead of being distributed across individual controllers.

### Test Users

The imported Keycloak realm provides three test users:

| Username | Realm Roles | Granted Scopes |
|---|---|---|
| `car-editor` | `CAR_EDITOR` | `cars:write` |
| `catalog-editor` | `CATALOG_EDITOR` | `catalog:write` |
| `car-tester` | `CAR_EDITOR`, `CATALOG_EDITOR` | `cars:write`, `catalog:write` |

Keycloak manages the mapping between realm roles and OAuth2 scopes. The application does not depend on Keycloak role names.

## API Documentation

OpenAPI 3 documentation is generated using Springdoc OpenAPI.

After starting the application, the documentation is available at:

- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

The OpenAPI specification documents the available endpoints, request and response schemas, HTTP status codes, and OAuth2 security requirements.

Public GET operations do not require authentication. Protected operations require the corresponding OAuth2 scopes.

Swagger UI supports OAuth2 authorization and JWT Bearer authentication for testing protected endpoints.

## Postman Integration Tests

The Postman collection is located at:

`docs/car-rest-service.integration-tests.postman_collection.json`

The collection contains integration tests for public API access, car operations, and catalog management.

Tests are organized into the following groups:

- **Public Access** — verifies public GET endpoints, request validation, and rejection of unauthenticated write operations.
- **Cars** — tests car creation, retrieval, updates, deletion, and business validation rules.
- **Catalog** — tests category, manufacturer, and car model management.

Postman scripts use collection variables to share data between requests, including generated IDs and other values required by subsequent operations.

### Postman Environment

The collection uses environment variables to separate configuration from individual requests.

The local environment is named `Car REST - Local` and contains:

| Variable | Example |
|---|---|
| `baseUrl` | `http://localhost:8080` |
| `keycloakUrl` | `http://localhost:8180` |
| `realm` | `car-service` |
| `clientId` | `car-rest-postman` |
| `callbackUrl` | `https://oauth.pstmn.io/v1/callback` |

Create or import an environment containing these variables and select it before running the collection.

Using environment variables allows the same collection to be reused with different API and Keycloak instances without modifying individual requests.

### Authentication

Authentication is managed through Postman's built-in OAuth2 authorization mechanism using Keycloak.

The collection uses **Authorization Code with PKCE** and the `car-rest-postman` public client.

The OAuth2 configuration is defined at the collection level. Protected requests use **Inherit auth from parent**, allowing a single access token to be reused across requests until it expires.

The **Public Access** folder explicitly uses **No Auth** to test unauthenticated access.

This approach keeps authentication centralized and avoids additional JavaScript scripts or collection variables for managing JWT tokens.

### Obtaining an Access Token

1. Import the collection into Postman.
2. Select the `Car REST - Local` environment.
3. Open the collection's **Authorization** tab.
4. Verify the following OAuth2 settings:

   | Setting | Value |
   |---|---|
   | Type | OAuth 2.0 |
   | Grant Type | Authorization Code (with PKCE) |
   | Auth URL | `{{keycloakUrl}}/realms/{{realm}}/protocol/openid-connect/auth` |
   | Access Token URL | `{{keycloakUrl}}/realms/{{realm}}/protocol/openid-connect/token` |
   | Client ID | `{{clientId}}` |
   | Client Secret | Not required (public client) |
   | Callback URL | `{{callbackUrl}}` |
   | Scope | `cars:write catalog:write` |
   | Add authorization data to | Request Headers |

5. Click **Get New Access Token**.
6. Authenticate using one of the imported Keycloak test users.
7. Click **Use Token** to apply the obtained token to the collection.

Postman attaches the access token to requests that inherit authorization from the collection:

```http
Authorization: Bearer <access_token>
```

The token is managed by Postman's OAuth2 authorization mechanism and does not need to be manually inserted into request headers or JavaScript scripts.

A new token must be obtained or the existing token refreshed when it expires.

### Running Integration Tests

Before running the collection:

1. Ensure PostgreSQL, Keycloak, and the Spring Boot application are running.
2. Select the correct Postman environment.
3. Obtain an OAuth2 access token using a test user with the required scopes.
4. Open the collection in Postman and select **Run**.
5. Execute the requests in their defined order.

For a complete run of the protected operations, the `car-tester` user can be used because it has both `cars:write` and `catalog:write` scopes.

The tests use Postman collection variables to pass data between dependent requests.

Some tests create temporary entities and subsequently update or delete them to verify API behavior.

The **Public Access** folder does not inherit OAuth2 authorization, ensuring that its unauthenticated access tests remain valid.

### Token and Credential Handling

The collection does not contain hardcoded JWT access tokens, refresh tokens, or user passwords.

OAuth2 authentication is handled by Postman, while JavaScript scripts are used only for integration test logic and request chaining.

Sensitive credentials and runtime tokens must not be committed to the repository.

This approach keeps the test collection reusable while maintaining a clear separation between authentication configuration and API test logic.
