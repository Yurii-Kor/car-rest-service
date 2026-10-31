# syntax=docker/dockerfile:1

# Build stage
FROM eclipse-temurin:21-jdk-noble AS build

WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

RUN chmod +x ./mvnw

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp dependency:go-offline

COPY src/main/ ./src/main/

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -ntp -Dmaven.test.skip=true package

# Runtime stage
FROM eclipse-temurin:21-jre-noble AS runtime

WORKDIR /app

RUN useradd --system --uid 10001 --create-home appuser

COPY --from=build --chown=appuser:appuser \
    /app/target/car-rest-service-0.0.1-SNAPSHOT.jar \
    /app/app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
