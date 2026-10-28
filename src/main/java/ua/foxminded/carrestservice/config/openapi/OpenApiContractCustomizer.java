package ua.foxminded.carrestservice.config.openapi;

import static ua.foxminded.carrestservice.config.openapi.OpenApiConfig.SECURITY_SCHEME;
import static ua.foxminded.carrestservice.config.openapi.OpenApiConfig.JWT_SECURITY_SCHEME;
import static ua.foxminded.carrestservice.security.ScopeConstants.CARS_WRITE;
import static ua.foxminded.carrestservice.security.ScopeConstants.CATALOG_WRITE;

import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Component;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.security.SecurityRequirement;

@Component
public class OpenApiContractCustomizer {

    public void configureOperationSecurity(OpenAPI openApi) {

        openApi.getPaths()
                .forEach(this::configurePath);
    }

    public void customizeProblemDetailSchema(OpenAPI openApi) {

        Optional.ofNullable(openApi.getComponents())
                .map(Components::getSchemas)
                .map(schemas -> schemas.get("ProblemDetail"))
                .map(Schema::getProperties)
                .ifPresent(this::customizeProblemDetailProperties);
    }

    private void configurePath(
            String path,
            PathItem pathItem) {

        pathItem.readOperationsMap()
                .forEach((method, operation) ->
                        addSecurityRequirement(
                                path, method, operation));
    }

    private void addSecurityRequirement(
            String path,
            PathItem.HttpMethod method,
            Operation operation) {

        if (method == PathItem.HttpMethod.GET
                || !path.startsWith("/api/v1/")) {
            return;
        }

        String scope = path.equals("/api/v1/cars")
                || path.startsWith("/api/v1/cars/")
                        ? CARS_WRITE
                        : CATALOG_WRITE;

        operation.addSecurityItem(
                new SecurityRequirement()
                        .addList(SECURITY_SCHEME, scope));

        operation.addSecurityItem(
                new SecurityRequirement()
                        .addList(JWT_SECURITY_SCHEME));
    }

    private void customizeProblemDetailProperties(
            Map<String, Schema> properties) {

        properties.remove("properties");

        Optional.ofNullable(properties.get("instance"))
                .ifPresent(instance ->
                        instance.setFormat("uri-reference"));

        properties.put(
                "errors",
                new ArraySchema()
                        .items(new StringSchema())
                        .description(
                                "Validation errors, present only when "
                                        + "request validation fails")
        );
    }
}