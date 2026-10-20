package ua.foxminded.carrestservice.web.carmodel;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.carmodel.CarModelControllerTestConstants.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.service.carmodel.CarModelService;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelCreateRequest;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelUpdateRequest;
import ua.foxminded.carrestservice.service.exception.EntityInUseException;

@WebMvcTest(CarModelController.class)
@ActiveProfiles("test")
class CarModelControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarModelService carModelService;

    @Test
    @DisplayName("create: duplicate model name -> returns 409 Conflict with error details")
    void create_duplicateName_returnsConflict() throws Exception {
        var message = "Car model already exists: manufacturerId=%d, name=%s"
                .formatted(TOYOTA_ID, COROLLA_NAME);

        when(carModelService.create(any(CarModelCreateRequest.class)))
                .thenThrow(new EntityExistsException(message));

        mockMvc.perform(post(MODELS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Corolla",
                            "manufacturerId": %d
                        }
                        """.formatted(TOYOTA_ID)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value(message));

        verify(carModelService).create(
                new CarModelCreateRequest(COROLLA_NAME, TOYOTA_ID));
    }

    @Test
    @DisplayName("create: unknown manufacturer -> returns 404 Not Found")
    void create_unknownManufacturer_returnsNotFound() throws Exception {
        when(carModelService.create(any(CarModelCreateRequest.class)))
                .thenThrow(new EntityNotFoundException(
                        "Manufacturer not found: id=%d"
                                .formatted(UNKNOWN_MANUFACTURER_ID)));

        mockMvc.perform(post(MODELS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Corolla",
                            "manufacturerId": %d
                        }
                        """.formatted(UNKNOWN_MANUFACTURER_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(carModelService).create(
                new CarModelCreateRequest(
                        COROLLA_NAME, UNKNOWN_MANUFACTURER_ID));
    }

    @Test
    @DisplayName("update: unknown model -> returns 404 Not Found")
    void update_unknownModel_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException(
                "Car model not found: id=%d".formatted(UNKNOWN_MODEL_ID)))
                .when(carModelService)
                .update(eq(UNKNOWN_MODEL_ID), any(CarModelUpdateRequest.class));

        mockMvc.perform(put(MODELS_URL + "/{id}", UNKNOWN_MODEL_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Corolla"}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(carModelService).update(
                UNKNOWN_MODEL_ID,
                new CarModelUpdateRequest(COROLLA_NAME));
    }

    @Test
    @DisplayName("update: duplicate model name -> returns 409 Conflict with error details")
    void update_duplicateName_returnsConflict() throws Exception {
        var message = "Car model already exists: manufacturerId=%d, name=%s"
                .formatted(TOYOTA_ID, CAMRY_NAME);

        doThrow(new EntityExistsException(message))
                .when(carModelService)
                .update(eq(COROLLA_ID), any(CarModelUpdateRequest.class));

        mockMvc.perform(put(MODELS_URL + "/{id}", COROLLA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Camry"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value(message));

        verify(carModelService).update(
                COROLLA_ID,
                new CarModelUpdateRequest(CAMRY_NAME));
    }

    @Test
    @DisplayName("delete: unknown model -> returns 404 Not Found")
    void delete_unknownModel_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException(
                "Car model not found: id=%d".formatted(UNKNOWN_MODEL_ID)))
                .when(carModelService)
                .delete(UNKNOWN_MODEL_ID);

        mockMvc.perform(delete(MODELS_URL + "/{id}", UNKNOWN_MODEL_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(carModelService).delete(UNKNOWN_MODEL_ID);
    }

    @Test
    @DisplayName("delete: model with cars -> returns 409 Conflict")
    void delete_modelWithCars_returnsConflict() throws Exception {
        var message = "Cannot delete car model with existing cars: id=%d"
                .formatted(COROLLA_ID);

        doThrow(new EntityInUseException(message))
                .when(carModelService)
                .delete(COROLLA_ID);

        mockMvc.perform(delete(MODELS_URL + "/{id}", COROLLA_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value(message));

        verify(carModelService).delete(COROLLA_ID);
    }

    @Test
    @DisplayName("delete: database constraint violation -> returns 409 Conflict with error details")
    void delete_databaseConstraintViolation_returnsConflict()
            throws Exception {

        doThrow(new DataIntegrityViolationException(
                "Foreign key constraint violation"))
                .when(carModelService)
                .delete(COROLLA_ID);

        mockMvc.perform(delete(MODELS_URL + "/{id}", COROLLA_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Operation violates a database constraint"));

        verify(carModelService).delete(COROLLA_ID);
    }
}
