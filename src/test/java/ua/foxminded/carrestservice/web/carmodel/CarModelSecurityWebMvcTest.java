package ua.foxminded.carrestservice.web.carmodel;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.carmodel.CarModelControllerTestConstants.*;

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
import ua.foxminded.carrestservice.service.carmodel.CarModelService;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelCreateRequest;
import ua.foxminded.carrestservice.testconfig.KeycloakTestConfig;
import ua.foxminded.carrestservice.testconfig.KeycloakTestTokens;

@WebMvcTest(CarModelController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, KeycloakTestConfig.class})
class CarModelSecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KeycloakTestTokens tokens;

    @MockitoBean
    private CarModelService carModelService;

    @Test
    @DisplayName("create: missing JWT -> returns 401 without calling service")
    void create_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post(MODELS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Yaris",
                            "manufacturerId": %d
                        }
                        """.formatted(TOYOTA_ID)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(carModelService);
    }

    @Test
    @DisplayName("create: JWT with cars:write -> returns 403")
    void create_withCarsScope_returnsForbidden() throws Exception {
        mockMvc.perform(post(MODELS_URL)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.carEditorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Yaris",
                            "manufacturerId": %d
                        }
                        """.formatted(TOYOTA_ID)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(carModelService);
    }

    @Test
    @DisplayName("create: JWT without write scopes -> returns 403")
    void create_withViewerToken_returnsForbidden() throws Exception {
        mockMvc.perform(post(MODELS_URL)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.viewerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Yaris",
                            "manufacturerId": %d
                        }
                        """.formatted(TOYOTA_ID)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(carModelService);
    }

    @Test
    @DisplayName("create: JWT with catalog:write -> reaches controller")
    void create_withCatalogScope_returnsCreated() throws Exception {
        when(carModelService.create(any(CarModelCreateRequest.class)))
                .thenReturn(COROLLA_ID);

        mockMvc.perform(post(MODELS_URL)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.catalogEditorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Yaris",
                            "manufacturerId": %d
                        }
                        """.formatted(TOYOTA_ID)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", MODELS_URL + "/" + COROLLA_ID))
                .andExpect(jsonPath("$.id").value(COROLLA_ID));

        verify(carModelService).create(
                new CarModelCreateRequest(YARIS_NAME, TOYOTA_ID));
    }
}
