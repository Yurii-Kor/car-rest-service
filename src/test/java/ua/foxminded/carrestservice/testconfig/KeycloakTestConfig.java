package ua.foxminded.carrestservice.testconfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

import dasniko.testcontainers.keycloak.KeycloakContainer;

@TestConfiguration(proxyBeanMethods = false)
public class KeycloakTestConfig {

    private static final String REALM = "car-service-test";

    private static final KeycloakContainer KEYCLOAK_CONTAINER =
            new KeycloakContainer(
                    "quay.io/keycloak/keycloak:26.8.0")
                    .withRealmImportFile(
                            "/keycloak/car-service-test-realm.json");

    static {
        KEYCLOAK_CONTAINER.start();
    }

    @Bean
    DynamicPropertyRegistrar keycloakProperties() {
        return registry -> {
            registry.add(
                    "spring.security.oauth2.resourceserver.jwt.issuer-uri",
                    () -> KEYCLOAK_CONTAINER.getIssuerUrl(REALM));

            registry.add(
                    "spring.security.oauth2.resourceserver.jwt.audiences[0]",
                    () -> "car-rest-api");
        };
    }

    @Bean
    KeycloakTestTokens keycloakTestTokens() {
        return new KeycloakTestTokens(KEYCLOAK_CONTAINER);
    }
}
