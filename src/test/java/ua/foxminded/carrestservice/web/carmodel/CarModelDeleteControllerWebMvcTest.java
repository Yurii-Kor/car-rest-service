
package ua.foxminded.carrestservice.web.carmodel;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.carmodel.CarModelControllerTestConstants.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.service.carmodel.CarModelService;
import ua.foxminded.carrestservice.service.exception.EntityInUseException;

@WebMvcTest(CarModelController.class)
@ActiveProfiles("test")
class CarModelDeleteControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarModelService carModelService;

    @Test
    @DisplayName("delete: existing model -> returns 204 No Content")
    void delete_existingModel_returnsNoContent() throws Exception {
        mockMvc.perform(delete(MODELS_URL + "/{id}", COROLLA_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(carModelService).delete(COROLLA_ID);
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
