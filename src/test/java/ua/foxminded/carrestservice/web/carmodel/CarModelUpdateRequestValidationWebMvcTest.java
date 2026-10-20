package ua.foxminded.carrestservice.web.carmodel;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ua.foxminded.carrestservice.service.carmodel.CarModelService;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelUpdateRequest;

@WebMvcTest(CarModelController.class)
@ActiveProfiles("test")
class CarModelUpdateRequestValidationWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarModelService carModelService;

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("validCases")
    @DisplayName("update: valid model name -> returns 204 No Content")
    void update_validRequest_returnsNoContent(
            String scenario,
            String name,
            String expectedName) throws Exception {

        mockMvc.perform(put(MODELS_URL + "/{id}", COROLLA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson("\"%s\"".formatted(name))))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(carModelService).update(
                COROLLA_ID,
                new CarModelUpdateRequest(expectedName));
    }

    static Stream<Arguments> validCases() {
        return Stream.of(
                Arguments.of(
                        "simple name",
                        COROLLA_NAME,
                        COROLLA_NAME),

                Arguments.of(
                        "name with spaces",
                        COROLLA_NAME + " Cross",
                        COROLLA_NAME + " Cross"),

                Arguments.of(
                        "one character",
                        "A",
                        "A"),

                Arguments.of(
                        "maximum length of 255",
                        "A".repeat(255),
                        "A".repeat(255)),

                Arguments.of(
                        "extra spaces -> normalized name",
                        "   %s    Cross   ".formatted(COROLLA_NAME),
                        COROLLA_NAME + " Cross"));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("invalidCases")
    @DisplayName("update: invalid model name -> returns 400 Bad Request")
    void update_invalidRequest_returnsBadRequest(
            String scenario,
            String json,
            String expectedMessage) throws Exception {

        mockMvc.perform(put(MODELS_URL + "/{id}", COROLLA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.errors", hasItem(allOf(
                        containsString("name"),
                        containsString(expectedMessage)))));

        verifyNoInteractions(carModelService);
    }

    static Stream<Arguments> invalidCases() {
        return Stream.of(
                Arguments.of(
                        "null name",
                        requestJson("null"),
                        "must not be blank"),

                Arguments.of(
                        "empty name",
                        requestJson("\"\""),
                        "must not be blank"),

                Arguments.of(
                        "blank name",
                        requestJson("\"   \\t   \""),
                        "must not be blank"),

                Arguments.of(
                        "length exceeds 255",
                        requestJson("\"" + "A".repeat(256) + "\""),
                        "size must be between 0 and 255"));
    }

    @Test
    @DisplayName("update: missing name -> returns 400 Bad Request")
    void update_missingName_returnsBadRequest() throws Exception {

        mockMvc.perform(put(MODELS_URL + "/{id}", COROLLA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItem(allOf(
                        containsString("name"),
                        containsString("must not be blank")))));

        verifyNoInteractions(carModelService);
    }

    private static String requestJson(String name) {
        return """
                {
                    "name": %s
                }
                """.formatted(name);
    }
}
