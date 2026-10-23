package ua.foxminded.carrestservice.web.car;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.car.CarControllerTestConstants.*;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ua.foxminded.carrestservice.security.SecurityConfig;
import ua.foxminded.carrestservice.service.car.CarQueryService;
import ua.foxminded.carrestservice.service.car.CarService;
import ua.foxminded.carrestservice.service.car.dto.CarCreateRequest;
import ua.foxminded.carrestservice.service.car.dto.CarUpdateRequest;
import ua.foxminded.carrestservice.testconfig.KeycloakTestConfig;
import ua.foxminded.carrestservice.testconfig.KeycloakTestTokens;

@WebMvcTest(CarController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, KeycloakTestConfig.class})
class CarSecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KeycloakTestTokens tokens;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private CarQueryService carQueryService;

    @Test
    @DisplayName("create: missing JWT -> returns 401 without calling service")
    void create_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": %d,
                            "modelId": %d,
                            "categoryIds": []
                        }
                        """.formatted(CAR_YEAR.getValue(), MODEL_ID)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(carService, carQueryService);
    }

    @Test
    @DisplayName("create: JWT with catalog:write -> returns 403")
    void create_withCatalogScope_returnsForbidden() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.catalogEditorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": %d,
                            "modelId": %d,
                            "categoryIds": []
                        }
                        """.formatted(CAR_YEAR.getValue(), MODEL_ID)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(carService, carQueryService);
    }

    @Test
    @DisplayName("create: viewer JWT -> returns 403")
    void create_withViewerToken_returnsForbidden() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.viewerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": %d,
                            "modelId": %d,
                            "categoryIds": []
                        }
                        """.formatted(CAR_YEAR.getValue(), MODEL_ID)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(carService, carQueryService);
    }

    @Test
    @DisplayName("create: JWT with cars:write -> returns 201 Created")
    void create_withCarsScope_returnsCreated() throws Exception {
        var request = new CarCreateRequest(
                CAR_YEAR,
                MODEL_ID,
                Set.of());

        when(carService.create(request)).thenReturn(CAR_ID);

        mockMvc.perform(post(BASE_URL)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.carEditorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": %d,
                            "modelId": %d,
                            "categoryIds": []
                        }
                        """.formatted(CAR_YEAR.getValue(), MODEL_ID)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", BASE_URL + "/" + CAR_ID))
                .andExpect(jsonPath("$.id").value(CAR_ID));

        verify(carService).create(request);
        verifyNoInteractions(carQueryService);
    }

    @Test
    @DisplayName("create: JWT with tampered signature -> returns 401")
    void create_withTamperedSignature_returnsUnauthorized() throws Exception {
        var validToken = tokens.carEditorToken();

        var signatureStart = validToken.lastIndexOf('.') + 1;

        var replacement = validToken.charAt(signatureStart) == 'A'
                ? 'B'
                : 'A';

        var invalidToken = validToken.substring(0, signatureStart)
                + replacement
                + validToken.substring(signatureStart + 1);

        mockMvc.perform(post(BASE_URL)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": %d,
                            "modelId": %d,
                            "categoryIds": []
                        }
                        """.formatted(CAR_YEAR.getValue(), MODEL_ID)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(carService, carQueryService);
    }

    @Test
    @DisplayName("create: JWT for another API -> returns 401")
    void create_withWrongAudience_returnsUnauthorized() throws Exception {
        var token = tokens.wrongAudienceCarEditorToken();

        mockMvc.perform(post(BASE_URL)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": %d,
                            "modelId": %d,
                            "categoryIds": []
                        }
                        """.formatted(CAR_YEAR.getValue(), MODEL_ID)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(carService, carQueryService);
    }

    @Test
    @DisplayName("update: JWT with catalog:write -> returns 403")
    void update_withCatalogScope_returnsForbidden() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.catalogEditorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"year": %d}
                        """.formatted(UPDATED_YEAR.getValue())))
                .andExpect(status().isForbidden());

        verifyNoInteractions(carService, carQueryService);
    }

    @Test
    @DisplayName("update: JWT with cars:write -> returns 204 No Content")
    void update_withCarsScope_returnsNoContent() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.carEditorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"year": %d}
                        """.formatted(UPDATED_YEAR.getValue())))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(carService).update(
                CAR_ID,
                new CarUpdateRequest(UPDATED_YEAR, null, null));
        verifyNoInteractions(carQueryService);
    }

    @Test
    @DisplayName("delete: JWT with catalog:write -> returns 403")
    void delete_withCatalogScope_returnsForbidden() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", CAR_ID)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.catalogEditorToken()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(carService, carQueryService);
    }

    @Test
    @DisplayName("delete: JWT with cars:write -> returns 204 No Content")
    void delete_withCarsScope_returnsNoContent() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", CAR_ID)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.carEditorToken()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(carService).delete(CAR_ID);
        verifyNoInteractions(carQueryService);
    }
}
