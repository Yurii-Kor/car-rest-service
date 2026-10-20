package ua.foxminded.carrestservice.web.car;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.car.CarControllerTestConstants.*;

import java.time.Year;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ua.foxminded.carrestservice.service.car.CarQueryService;
import ua.foxminded.carrestservice.service.car.CarService;
import ua.foxminded.carrestservice.service.car.dto.CarUpdateRequest;

@WebMvcTest(CarController.class)
@ActiveProfiles("test")
class CarUpdateRequestValidationWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private CarQueryService carQueryService;

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("validCases")
    @DisplayName("update: valid request -> returns 204 and passes expected DTO")
    void update_validRequest_returnsNoContent(
            String scenario,
            String json,
            CarUpdateRequest expected) throws Exception {

        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(carService).update(CAR_ID, expected);
    }

    static Stream<Arguments> validCases() {
        var currentYear = Year.now();
    
        return Stream.of(
                Arguments.of(
                        "minimum year",
                        """
                        {"year": 1900}
                        """,
                        new CarUpdateRequest(
                                Year.of(1900), null, null)),
    
                Arguments.of(
                        "current year",
                        """
                        {"year": %d}
                        """.formatted(currentYear.getValue()),
                        new CarUpdateRequest(
                                currentYear, null, null)),
    
                Arguments.of(
                        "all fields explicitly null",
                        """
                        {
                            "year": null,
                            "modelId": null,
                            "categoryIds": null
                        }
                        """,
                        new CarUpdateRequest(null, null, null)),
    
                Arguments.of(
                        "duplicate category IDs",
                        """
                        {
                            "categoryIds": [%d, %d, %d]
                        }
                        """.formatted(
                                SEDAN_ID,
                                SEDAN_ID,
                                SUV_ID),
                        new CarUpdateRequest(
                                null,
                                null,
                                Set.of(SEDAN_ID, SUV_ID))));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("invalidCases")
    @DisplayName("update: invalid request -> returns 400 with field validation error")
    void update_invalidRequest_returnsBadRequest(
            String scenario,
            String json,
            String expectedField,
            String expectedMessage) throws Exception {

        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
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
        return Stream.of(
                Arguments.of(
                        "year before 1900",
                        """
                        {"year": 1899}
                        """,
                        "year",
                        "Year must not be earlier than 1900"),

                Arguments.of(
                        "year zero",
                        """
                        {"year": 0}
                        """,
                        "year",
                        "Year must not be earlier than 1900"),

                Arguments.of(
                        "future year",
                        """
                        {"year": %d}
                        """.formatted(
                                Year.now().plusYears(1).getValue()),
                        "year",
                        "must be a date in the past or in the present"),

                Arguments.of(
                        "null category element",
                        """
                        {"categoryIds": [%d, null]}
                        """.formatted(SEDAN_ID),
                        "categoryIds",
                        "must not be null"),

                Arguments.of(
                        "zero model ID",
                        """
                        {"modelId": 0}
                        """,
                        "modelId",
                        "must be greater than 0"),

                Arguments.of(
                        "negative model ID",
                        """
                        {"modelId": -1}
                        """,
                        "modelId",
                        "must be greater than 0"),

                Arguments.of(
                        "negative category ID",
                        """
                        {"categoryIds": [-1]}
                        """,
                        "categoryIds",
                        "must be greater than 0"));
    }
}
