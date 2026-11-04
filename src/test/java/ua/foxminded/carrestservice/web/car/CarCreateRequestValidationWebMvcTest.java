package ua.foxminded.carrestservice.web.car;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.car.CarControllerTestConstants.*;

import java.time.Year;
import java.util.Set;
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
import ua.foxminded.carrestservice.service.car.CarQueryService;
import ua.foxminded.carrestservice.service.car.CarService;
import ua.foxminded.carrestservice.service.car.dto.CarCreateRequest;

@WebMvcTest(CarController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class CarCreateRequestValidationWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private CarQueryService carQueryService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("validCases")
    @DisplayName("create: valid year boundaries -> returns 201 Created")
    void create_validRequest_returnsCreated(
            String scenario, Year year) throws Exception {

        when(carService.create(any(CarCreateRequest.class)))
                .thenReturn(CAR_ID);

        mockMvc.perform(post(BASE_URL)
                .with(carEditor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson(
                        Integer.toString(year.getValue()),
                        MODEL_ID.toString(),
                        "[%d]".formatted(SEDAN_ID))))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", BASE_URL + "/" + CAR_ID))
                .andExpect(jsonPath("$.id").value(CAR_ID));

        verify(carService).create(new CarCreateRequest(
                year,
                MODEL_ID,
                Set.of(SEDAN_ID)));
    }

    static Stream<Arguments> validCases() {
        return Stream.of(
                Arguments.of("minimum year", Year.of(1900)),
                Arguments.of("current year", Year.now()));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("invalidCases")
    @DisplayName("create: invalid request -> returns 400 with field validation error")
    void create_invalidRequest_returnsBadRequest(
            String scenario,
            String json,
            String expectedField,
            String expectedMessage) throws Exception {

        mockMvc.perform(post(BASE_URL)
                .with(carEditor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.errors", hasItem(allOf(
                        containsString(expectedField),
                        containsString(expectedMessage)))));

        verifyNoInteractions(carService);
    }

    static Stream<Arguments> invalidCases() {
        var year = Integer.toString(CAR_YEAR.getValue());
        var modelId = MODEL_ID.toString();
        var categories = "[%d]".formatted(SEDAN_ID);

        return Stream.of(
                Arguments.of(
                        "null year",
                        requestJson("null", modelId, categories),
                        "year",
                        "must not be null"),

                Arguments.of(
                        "year before 1900",
                        requestJson("1899", modelId, categories),
                        "year",
                        "Year must not be earlier than 1900"),

                Arguments.of(
                        "year zero",
                        requestJson("0", modelId, categories),
                        "year",
                        "Year must not be earlier than 1900"),

                Arguments.of(
                        "future year",
                        requestJson(
                                Integer.toString(
                                        Year.now().plusYears(1).getValue()),
                                modelId,
                                categories),
                        "year",
                        "must be a date in the past or in the present"),

                Arguments.of(
                        "null model ID",
                        requestJson(year, "null", categories),
                        "modelId",
                        "must not be null"),

                Arguments.of(
                        "missing model ID",
                        """
                        {
                            "year": %d,
                            "categoryIds": [%d]
                        }
                        """.formatted(CAR_YEAR.getValue(), SEDAN_ID),
                        "modelId",
                        "must not be null"),

                Arguments.of(
                        "null category set",
                        requestJson(year, modelId, "null"),
                        "categoryIds",
                        "must not be null"),

                Arguments.of(
                        "null category element",
                        requestJson(year, modelId, "[%d, null]"
                                .formatted(SEDAN_ID)),
                        "categoryIds",
                        "must not be null"),

                Arguments.of(
                        "zero model ID",
                        requestJson(year, "0", categories),
                        "modelId",
                        "must be greater than 0"),

                Arguments.of(
                        "negative model ID",
                        requestJson(year, "-1", categories),
                        "modelId",
                        "must be greater than 0"),

                Arguments.of(
                        "negative category ID",
                        requestJson(year, modelId, "[-1]"),
                        "categoryIds",
                        "must be greater than 0"));
    }

    @Test
    @DisplayName("create: all fields null -> returns validation errors for all fields")
    void create_allFieldsNull_returnsBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .with(carEditor())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson("null", "null", "null")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors", hasItems(
                        containsString("year:"),
                        containsString("modelId:"),
                        containsString("categoryIds:"))));

        verifyNoInteractions(carService);
    }

    private static RequestPostProcessor carEditor() {
        return jwt().authorities(
                new SimpleGrantedAuthority("SCOPE_cars:write"));
    }

    private static String requestJson(
            String year,
            String modelId,
            String categoryIds) {

        return """
                {
                    "year": %s,
                    "modelId": %s,
                    "categoryIds": %s
                }
                """.formatted(year, modelId, categoryIds);
    }
}
