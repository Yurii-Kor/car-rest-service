package ua.foxminded.carrestservice.testconfig;

import dasniko.testcontainers.keycloak.KeycloakContainer;

public class KeycloakTestTokens {

    private static final String REALM = "car-service-test";
    private static final String CLIENT_ID = "car-rest-test";
    private static final String OTHER_AUDIENCE_CLIENT_ID =
            "car-rest-other-audience";

    private final KeycloakContainer keycloakContainer;

    public KeycloakTestTokens(KeycloakContainer keycloakContainer) {
        this.keycloakContainer = keycloakContainer;
    }

    public String carEditorToken() {
        return accessToken(
                CLIENT_ID,
                "car-editor",
                "car-editor-test-password");
    }

    public String catalogEditorToken() {
        return accessToken(
                CLIENT_ID,
                "catalog-editor",
                "catalog-editor-test-password");
    }

    public String viewerToken() {
        return accessToken(
                CLIENT_ID,
                "viewer",
                "viewer-test-password");
    }

    public String wrongAudienceCarEditorToken() {
        return accessToken(
                OTHER_AUDIENCE_CLIENT_ID,
                "car-editor",
                "car-editor-test-password");
    }

    private String accessToken(
            String clientId,
            String username,
            String password) {

        return keycloakContainer.getAccessToken(
                REALM,
                clientId,
                username,
                password);
    }
}
