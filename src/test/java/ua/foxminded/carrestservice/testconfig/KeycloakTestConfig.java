package ua.foxminded.carrestservice.testconfig;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;

import dasniko.testcontainers.keycloak.KeycloakContainer;

@TestConfiguration(proxyBeanMethods = false)
public class KeycloakTestConfig {

    private static final String REALM = "car-service-test";

    @Bean
    KeycloakContainer keycloakContainer() {
        return new KeycloakContainer(
                "quay.io/keycloak/keycloak:26.8.0")
                .withRealmImportFile(
                        "/keycloak/car-service-test-realm.json");
    }

    @Bean
    DynamicPropertyRegistrar keycloakProperties(
            KeycloakContainer keycloakContainer) {

        return registry -> {
            registry.add(
                    "spring.security.oauth2.resourceserver.jwt.issuer-uri",
                    () -> keycloakContainer.getIssuerUrl(REALM));

            registry.add(
                    "spring.security.oauth2.resourceserver.jwt.audiences[0]",
                    () -> "car-rest-api");
        };
    }

    @Bean
    KeycloakTestTokens keycloakTestTokens(
            KeycloakContainer keycloakContainer) {

        return new KeycloakTestTokens(keycloakContainer);
    }
}
