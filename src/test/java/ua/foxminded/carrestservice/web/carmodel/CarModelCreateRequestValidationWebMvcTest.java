package ua.foxminded.carrestservice.web.carmodel;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.carmodel.CarModelControllerTestConstants.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import ua.foxminded.carrestservice.security.SecurityConfig;
import ua.foxminded.carrestservice.service.carmodel.CarModelService;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelCreateRequest;

@WebMvcTest(CarModelController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class CarModelCreateRequestValidationWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarModelService carModelService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("validCases")
    @DisplayName("create: valid model request -> returns 201 Created")
    void create_validRequest_returnsCreated(
            String scenario,
            String name,
            Long manufacturerId,
            String expectedName) throws Exception {

        when(carModelService.create(any(CarModelCreateRequest.class)))
                .thenReturn(COROLLA_ID);

        mockMvc.perform(post(MODELS_URL)
                .with(catalogEditor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "%s",
                            "manufacturerId": %d
                        }
                        """.formatted(name, manufacturerId)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", MODELS_URL + "/" + COROLLA_ID))
                .andExpect(jsonPath("$.id").value(COROLLA_ID));

        verify(carModelService).create(
                new CarModelCreateRequest(expectedName, manufacturerId));
    }

    static Stream<Arguments> validCases() {
        return Stream.of(
                Arguments.of(
                        "one character",
                        "A",
                        TOYOTA_ID,
                        "A"),

                Arguments.of(
                        "maximum length of 255",
                        "A".repeat(255),
                        TOYOTA_ID,
                        "A".repeat(255)),

                Arguments.of(
                        "minimum positive manufacturer ID",
                        COROLLA_NAME,
                        1L,
                        COROLLA_NAME),

                Arguments.of(
                        "extra spaces -> normalized name",
                        "  Corolla   Cross  ",
                        TOYOTA_ID,
                        "Corolla Cross"));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("invalidCases")
    @DisplayName("create: invalid model request -> returns 400 Bad Request")
    void create_invalidRequest_returnsBadRequest(
            String scenario,
            String json,
            String expectedField,
            String expectedMessage) throws Exception {

        mockMvc.perform(post(MODELS_URL)
                .with(catalogEditor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.errors", hasItem(allOf(
                        containsString(expectedField),
                        containsString(expectedMessage)))));

        verifyNoInteractions(carModelService);
    }

    static Stream<Arguments> invalidCases() {
        var validName = "\"%s\"".formatted(COROLLA_NAME);
        var validManufacturerId = TOYOTA_ID.toString();

        return Stream.of(
                Arguments.of(
                        "null name",
                        requestJson("null", validManufacturerId),
                        "name",
                        "must not be blank"),

                Arguments.of(
                        "empty name",
                        requestJson("\"\"", validManufacturerId),
                        "name",
                        "must not be blank"),

                Arguments.of(
                        "blank name",
                        requestJson("\"   \\t   \"", validManufacturerId),
                        "name",
                        "must not be blank"),

                Arguments.of(
                        "length exceeds 255",
                        requestJson(
                                "\"" + "A".repeat(256) + "\"",
                                validManufacturerId),
                        "name",
                        "size must be between 0 and 255"),

                Arguments.of(
                        "null manufacturer ID",
                        requestJson(validName, "null"),
                        "manufacturerId",
                        "must not be null"),

                Arguments.of(
                        "missing manufacturer ID",
                        """
                        {
                            "name": "%s"
                        }
                        """.formatted(COROLLA_NAME),
                        "manufacturerId",
                        "must not be null"),

                Arguments.of(
                        "zero manufacturer ID",
                        requestJson(validName, "0"),
                        "manufacturerId",
                        "must be greater than 0"),

                Arguments.of(
                        "negative manufacturer ID",
                        requestJson(validName, "-1"),
                        "manufacturerId",
                        "must be greater than 0"));
    }

    @Test
    @DisplayName("create: all fields null -> returns errors for both fields")
    void create_allFieldsNull_returnsBadRequest() throws Exception {

        mockMvc.perform(post(MODELS_URL)
                .with(catalogEditor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson("null", "null")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItems(
                        containsString("name"),
                        containsString("manufacturerId"))));

        verifyNoInteractions(carModelService);
    }

    private static RequestPostProcessor catalogEditor() {
        return jwt().authorities(
                new SimpleGrantedAuthority("SCOPE_catalog:write"));
    }

    private static String requestJson(
            String name,
            String manufacturerId) {

        return """
                {
                    "name": %s,
                    "manufacturerId": %s
                }
                """.formatted(name, manufacturerId);
    }
}
