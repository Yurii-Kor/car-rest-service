package ua.foxminded.carrestservice.web.car;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.car.CarControllerTestConstants.*;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.security.SecurityConfig;
import ua.foxminded.carrestservice.service.car.CarQueryService;
import ua.foxminded.carrestservice.service.car.CarService;
import ua.foxminded.carrestservice.service.car.dto.CarCreateRequest;
import ua.foxminded.carrestservice.service.car.dto.CarUpdateRequest;
import ua.foxminded.carrestservice.service.car.exception.CarManufacturerChangeNotAllowedException;

@WebMvcTest(CarController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class CarControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private CarQueryService carQueryService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("create: empty category set -> returns 201 Created")
    void create_emptyCategories_returnsCreated() throws Exception {
        var request = new CarCreateRequest(
                CAR_YEAR,
                MODEL_ID,
                Set.of());

        when(carService.create(request)).thenReturn(CAR_ID);

        mockMvc.perform(post(BASE_URL)
                .with(carEditor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": %d,
                            "modelId": %d,
                            "categoryIds": []
                        }
                        """.formatted(CAR_YEAR.getValue(), MODEL_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(CAR_ID));

        verify(carService).create(request);
    }

    @Test
    @DisplayName("create: duplicate model and year -> returns 409 Conflict")
    void create_duplicateCombination_returnsConflict() throws Exception {
        var request = new CarCreateRequest(
                CAR_YEAR,
                MODEL_ID,
                Set.of(SEDAN_ID));

        var message = "Car already exists: modelId=%d, year=%d"
                .formatted(MODEL_ID, CAR_YEAR.getValue());

        when(carService.create(request))
                .thenThrow(new EntityExistsException(message));

        mockMvc.perform(post(BASE_URL)
                .with(carEditor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": %d,
                            "modelId": %d,
                            "categoryIds": [%d]
                        }
                        """.formatted(
                                CAR_YEAR.getValue(),
                                MODEL_ID,
                                SEDAN_ID)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value(message));

        verify(carService).create(request);
    }

    @Test
    @DisplayName("create: unknown model -> returns 404 Not Found")
    void create_unknownModel_returnsNotFound() throws Exception {
        var request = new CarCreateRequest(
                CAR_YEAR,
                UNKNOWN_MODEL_ID,
                Set.of(SEDAN_ID));

        var message = "Car model not found: id=%d"
                .formatted(UNKNOWN_MODEL_ID);

        when(carService.create(request))
                .thenThrow(new EntityNotFoundException(message));

        mockMvc.perform(post(BASE_URL)
                .with(carEditor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": %d,
                            "modelId": %d,
                            "categoryIds": [%d]
                        }
                        """.formatted(
                                CAR_YEAR.getValue(),
                                UNKNOWN_MODEL_ID,
                                SEDAN_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(carService).create(request);
    }

    @Test
    @DisplayName("update: model only -> passes partial request to service")
    void update_modelOnly_returnsNoContent() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .with(carEditor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"modelId": %d}
                        """.formatted(SECOND_MODEL_ID)))
                .andExpect(status().isNoContent());

        verify(carService).update(
                CAR_ID,
                new CarUpdateRequest(null, SECOND_MODEL_ID, null));
    }

    @Test
    @DisplayName("update: empty category array -> passes empty set to service")
    void update_emptyCategories_passesEmptySet() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .with(carEditor())
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
                .with(carEditor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"categoryIds": [%d, %d]}
                        """.formatted(SEDAN_ID, SUV_ID)))
                .andExpect(status().isNoContent());

        verify(carService).update(
                CAR_ID,
                new CarUpdateRequest(
                        null, null, Set.of(SEDAN_ID, SUV_ID)));
    }

    @Test
    @DisplayName("update: empty JSON object -> returns 400 Bad Request")
    void update_emptyRequest_returnsBadRequest() throws Exception {
    
        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .with(carEditor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.errors[0]")
                        .value("anyFieldProvided: At least one field must be provided"));
    
        verifyNoInteractions(carService);
    }

    @Test
    @DisplayName("update: unknown car -> returns 404 Not Found")
    void update_unknownCar_returnsNotFound() throws Exception {
        var request = new CarUpdateRequest(UPDATED_YEAR, null, null);

        var message = "Car not found: id=%d"
                .formatted(UNKNOWN_CAR_ID);

        doThrow(new EntityNotFoundException(message))
                .when(carService)
                .update(UNKNOWN_CAR_ID, request);

        mockMvc.perform(patch(BASE_URL + "/{id}", UNKNOWN_CAR_ID)
                .with(carEditor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"year": %d}
                        """.formatted(UPDATED_YEAR.getValue())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(carService).update(UNKNOWN_CAR_ID, request);
    }

    @Test
    @DisplayName("update: duplicate model and year -> returns 409 Conflict")
    void update_duplicateCombination_returnsConflict() throws Exception {
        var request = new CarUpdateRequest(UPDATED_YEAR, null, null);

        var message = "Car already exists: modelId=%d, year=%d"
                .formatted(MODEL_ID, UPDATED_YEAR.getValue());

        doThrow(new EntityExistsException(message))
                .when(carService)
                .update(CAR_ID, request);

        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .with(carEditor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"year": %d}
                        """.formatted(UPDATED_YEAR.getValue())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value(message));

        verify(carService).update(CAR_ID, request);
    }

    @Test
    @DisplayName("update: model belongs to another manufacturer -> returns 409 Conflict")
    void update_differentManufacturer_returnsConflict() throws Exception {
        var request = new CarUpdateRequest(null, SECOND_MODEL_ID, null);

        var message = "Cannot change car manufacturer: carId=%d, currentManufacturerId=%d, requestedManufacturerId=%d"
                .formatted(
                        CAR_ID,
                        MANUFACTURER_ID,
                        OTHER_MANUFACTURER_ID);

        doThrow(new CarManufacturerChangeNotAllowedException(
                CAR_ID,
                MANUFACTURER_ID,
                OTHER_MANUFACTURER_ID))
                .when(carService)
                .update(CAR_ID, request);

        mockMvc.perform(patch(BASE_URL + "/{id}", CAR_ID)
                .with(carEditor())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"modelId": %d}
                        """.formatted(SECOND_MODEL_ID)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value(message));

        verify(carService).update(CAR_ID, request);
    }

    @Test
    @DisplayName("delete: unknown car -> returns 404 Not Found")
    void delete_unknownCar_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException(
                "Car not found: id=%d".formatted(UNKNOWN_CAR_ID)))
                .when(carService)
                .delete(UNKNOWN_CAR_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", UNKNOWN_CAR_ID)
                .with(carEditor()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(carService).delete(UNKNOWN_CAR_ID);
    }

    @Test
    @DisplayName("delete: database constraint violation -> returns 409 Conflict")
    void delete_databaseConstraintViolation_returnsConflict()
            throws Exception {

        doThrow(new DataIntegrityViolationException(
                "Database constraint violation"))
                .when(carService)
                .delete(CAR_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", CAR_ID)
                .with(carEditor()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Operation violates a database constraint"));

        verify(carService).delete(CAR_ID);
    }

    private static RequestPostProcessor carEditor() {
        return jwt().authorities(
                new SimpleGrantedAuthority("SCOPE_cars:write"));
    }
}
