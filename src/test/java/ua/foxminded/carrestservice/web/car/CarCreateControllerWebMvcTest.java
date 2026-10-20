package ua.foxminded.carrestservice.web.car;

import static org.mockito.Mockito.*;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.service.car.CarQueryService;
import ua.foxminded.carrestservice.service.car.CarService;
import ua.foxminded.carrestservice.service.car.dto.CarCreateRequest;

@WebMvcTest(CarController.class)
@ActiveProfiles("test")
class CarCreateControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private CarQueryService carQueryService;

    @Test
    @DisplayName("create: valid car request -> returns 201 Created and Location header")
    void create_validRequest_returnsCreatedAndLocation() throws Exception {
        var request = new CarCreateRequest(
                CAR_YEAR,
                MODEL_ID,
                Set.of(SEDAN_ID, SUV_ID));

        when(carService.create(request)).thenReturn(CAR_ID);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": 2020,
                            "modelId": 201,
                            "categoryIds": [401, 402]
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", BASE_URL + "/" + CAR_ID))
                .andExpect(jsonPath("$.id").value(CAR_ID));

        verify(carService).create(request);
    }

    @Test
    @DisplayName("create: empty category set -> returns 201 Created")
    void create_emptyCategories_returnsCreated() throws Exception {
        var request = new CarCreateRequest(
                CAR_YEAR,
                MODEL_ID,
                Set.of());

        when(carService.create(request)).thenReturn(CAR_ID);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": 2020,
                            "modelId": 201,
                            "categoryIds": []
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(CAR_ID));

        verify(carService).create(request);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("invalidYears")
    @DisplayName("create: invalid production year -> returns 400 Bad Request")
    void create_invalidYear_returnsBadRequest(
            String scenario, int year) throws Exception {

        var json = """
                {
                    "year": %d,
                    "modelId": 201,
                    "categoryIds": [401]
                }
                """.formatted(year);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0]").exists());

        verifyNoInteractions(carService);
    }

    static Stream<Arguments> invalidYears() {
        return Stream.of(
                Arguments.of("year before 1900", 1899),
                Arguments.of("future year", Year.now().plusYears(1).getValue()));
    }

    @Test
    @DisplayName("create: missing model ID -> returns 400 Bad Request")
    void create_missingModelId_returnsBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": 2020,
                            "categoryIds": []
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(carService);
    }

    @Test
    @DisplayName("create: null category element -> returns 400 Bad Request")
    void create_nullCategoryElement_returnsBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": 2020,
                            "modelId": 201,
                            "categoryIds": [401, null]
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(carService);
    }

    @Test
    @DisplayName("create: duplicate model and year -> returns 409 Conflict")
    void create_duplicateCombination_returnsConflict() throws Exception {
        var request = new CarCreateRequest(
                CAR_YEAR,
                MODEL_ID,
                Set.of(SEDAN_ID));

        when(carService.create(request))
                .thenThrow(new EntityExistsException(
                        "Car already exists: modelId=201, year=2020"));

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": 2020,
                            "modelId": 201,
                            "categoryIds": [401]
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Car already exists: modelId=201, year=2020"));

        verify(carService).create(request);
    }

    @Test
    @DisplayName("create: unknown model -> returns 404 Not Found")
    void create_unknownModel_returnsNotFound() throws Exception {
        var request = new CarCreateRequest(
                CAR_YEAR,
                UNKNOWN_MODEL_ID,
                Set.of(SEDAN_ID));

        when(carService.create(request))
                .thenThrow(new EntityNotFoundException(
                        "Car model not found: id=%d"
                                .formatted(UNKNOWN_MODEL_ID)));

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": 2020,
                            "modelId": 999998,
                            "categoryIds": [401]
                        }
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(carService).create(request);
    }
}
