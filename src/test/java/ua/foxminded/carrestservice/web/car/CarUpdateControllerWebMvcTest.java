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
import ua.foxminded.carrestservice.service.car.dto.CarUpdateRequest;
import ua.foxminded.carrestservice.service.car.exception.CarManufacturerChangeNotAllowedException;

@WebMvcTest(CarController.class)
@ActiveProfiles("test")
class CarUpdateControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private CarQueryService carQueryService;

    @Test
    @DisplayName("update: year only -> returns 204 No Content with partial request")
    void update_yearOnly_returnsNoContent() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"year": 2021}
                        """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(carService).update(
                CAR_ID,
                new CarUpdateRequest(UPDATED_YEAR, null, null));
    }

    @Test
    @DisplayName("update: model only -> returns 204 No Content with partial request")
    void update_modelOnly_returnsNoContent() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"modelId": 202}
                        """))
                .andExpect(status().isNoContent());

        verify(carService).update(
                CAR_ID,
                new CarUpdateRequest(null, SECOND_MODEL_ID, null));
    }

    @Test
    @DisplayName("update: empty category array -> passes empty set to service")
    void update_emptyCategories_passesEmptySet() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"categoryIds": []}
                        """))
                .andExpect(status().isNoContent());

        verify(carService).update(
                CAR_ID,
                new CarUpdateRequest(null, null, Set.of()));
    }

    @Test
    @DisplayName("update: category IDs provided -> passes replacement set to service")
    void update_categoriesOnly_passesReplacementSet() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"categoryIds": [401, 402]}
                        """))
                .andExpect(status().isNoContent());

        verify(carService).update(
                CAR_ID,
                new CarUpdateRequest(
                        null, null, Set.of(SEDAN_ID, SUV_ID)));
    }

    @Test
    @DisplayName("update: empty JSON object -> passes all-null request to service")
    void update_emptyRequest_passesNullFields() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {}
                        """))
                .andExpect(status().isNoContent());

        verify(carService).update(
                CAR_ID,
                new CarUpdateRequest(null, null, null));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("invalidYears")
    @DisplayName("update: invalid production year -> returns 400 Bad Request")
    void update_invalidYear_returnsBadRequest(
            String scenario, int year) throws Exception {

        var json = """
                {"year": %d}
                """.formatted(year);

        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
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
    @DisplayName("update: null category element -> returns 400 Bad Request")
    void update_nullCategoryElement_returnsBadRequest() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"categoryIds": [401, null]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(carService);
    }

    @Test
    @DisplayName("update: unknown car -> returns 404 Not Found")
    void update_unknownCar_returnsNotFound() throws Exception {
        var request = new CarUpdateRequest(UPDATED_YEAR, null, null);

        doThrow(new EntityNotFoundException(
                "Car not found: id=%d".formatted(UNKNOWN_CAR_ID)))
                .when(carService)
                .update(UNKNOWN_CAR_ID, request);

        mockMvc.perform(patch(BASE_URL + "/{id}", UNKNOWN_CAR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"year": 2021}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(carService).update(UNKNOWN_CAR_ID, request);
    }

    @Test
    @DisplayName("update: duplicate model and year -> returns 409 Conflict")
    void update_duplicateCombination_returnsConflict() throws Exception {
        var request = new CarUpdateRequest(UPDATED_YEAR, null, null);

        doThrow(new EntityExistsException(
                "Car already exists: modelId=201, year=2021"))
                .when(carService)
                .update(CAR_ID, request);

        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"year": 2021}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Car already exists: modelId=201, year=2021"));

        verify(carService).update(CAR_ID, request);
    }

    @Test
    @DisplayName("update: model belongs to another manufacturer -> returns 409 Conflict")
    void update_differentManufacturer_returnsConflict() throws Exception {
        var request = new CarUpdateRequest(null, SECOND_MODEL_ID, null);

        doThrow(new CarManufacturerChangeNotAllowedException(
                CAR_ID,
                MANUFACTURER_ID,
                OTHER_MANUFACTURER_ID))
                .when(carService)
                .update(CAR_ID, request);

        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"modelId": 202}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Cannot change car manufacturer: carId=301, currentManufacturerId=101, requestedManufacturerId=102"));

        verify(carService).update(CAR_ID, request);
    }
}
