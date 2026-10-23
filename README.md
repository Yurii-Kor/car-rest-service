# Car REST Service

A RESTful web application for managing and querying car catalog data, built with Java and Spring Boot.

## Tech Stack

- Java & Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Flyway Database Migrations
- Docker Compose
- Maven

## Project Status

**Under development**

Initial project setup, database schema, and CSV data import are completed.

Further functionality and documentation will be added as development progresses.

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

All six variables are required for the local development setup.

Docker Compose reads the `.env` file to configure PostgreSQL and Keycloak. Spring Boot imports the same file through `spring.config.import` in `application.yaml`.

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

The local Keycloak instance must have the `car-service` realm configured.

**Note:** The Docker Compose configuration uses an external PostgreSQL volume named `car-rest-postgres-data`. For a fresh installation, create it before starting the containers:

```powershell
docker volume create car-rest-postgres-data
```
