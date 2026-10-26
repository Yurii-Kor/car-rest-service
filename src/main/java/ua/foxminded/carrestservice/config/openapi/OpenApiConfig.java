package ua.foxminded.carrestservice.config.openapi;

import static ua.foxminded.carrestservice.security.ScopeConstants.CARS_WRITE;
import static ua.foxminded.carrestservice.security.ScopeConstants.CATALOG_WRITE;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfig {

    static final String SECURITY_SCHEME = "keycloak";

    private final OpenApiContractCustomizer customizer;

    @Bean
    OpenAPI openAPI(
            @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
            String issuerUri) {

        var oauthFlow = new OAuthFlow()
                .authorizationUrl(
                        issuerUri + "/protocol/openid-connect/auth")
                .tokenUrl(
                        issuerUri + "/protocol/openid-connect/token")
                .scopes(new Scopes()
                        .addString(CARS_WRITE, "Manage cars")
                        .addString(CATALOG_WRITE, "Manage catalog data"));

        var securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.OAUTH2)
                .flows(new OAuthFlows()
                        .authorizationCode(oauthFlow));

        return new OpenAPI()
                .info(new Info()
                        .title("Car REST Service API")
                        .version("v1")
                        .description(
                                "REST API for managing cars, manufacturers, models and categories"))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME, securityScheme));
    }

    @Bean
    OpenApiCustomizer operationSecurityCustomizer() {
        return customizer::configureOperationSecurity;
    }

    @Bean
    OpenApiCustomizer problemDetailSchemaCustomizer() {
        return customizer::customizeProblemDetailSchema;
    }
}