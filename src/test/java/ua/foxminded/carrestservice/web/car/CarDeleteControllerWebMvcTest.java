package ua.foxminded.carrestservice.web.car;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.car.CarControllerTestConstants.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.service.car.CarQueryService;
import ua.foxminded.carrestservice.service.car.CarService;

@WebMvcTest(CarController.class)
@ActiveProfiles("test")
class CarDeleteControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarService carService;

    @MockitoBean
    private CarQueryService carQueryService;

    @Test
    @DisplayName("delete: existing car -> returns 204 No Content")
    void delete_existingCar_returnsNoContent() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", CAR_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(carService).delete(CAR_ID);
    }

    @Test
    @DisplayName("delete: unknown car -> returns 404 Not Found")
    void delete_unknownCar_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException(
                "Car not found: id=%d".formatted(UNKNOWN_CAR_ID)))
                .when(carService)
                .delete(UNKNOWN_CAR_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", UNKNOWN_CAR_ID))
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

        mockMvc.perform(delete(BASE_URL + "/{id}", CAR_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Operation violates a database constraint"));

        verify(carService).delete(CAR_ID);
    }
}
