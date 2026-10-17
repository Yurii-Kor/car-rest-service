package ua.foxminded.carrestservice.web.manufacturer;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.manufacturer.ManufacturerControllerTestConstants.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.service.manufacturer.ManufacturerService;

@WebMvcTest(ManufacturerController.class)
@ActiveProfiles("test")
class ManufacturerDeleteControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManufacturerService manufacturerService;

    @Test
    @DisplayName("delete: existing manufacturer -> returns 204 No Content")
    void delete_existingManufacturer_returnsNoContent() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", TOYOTA_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(manufacturerService).delete(TOYOTA_ID);
    }

    @Test
    @DisplayName("delete: unknown manufacturer -> returns 404 Not Found")
    void delete_unknownManufacturer_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException(
                "Manufacturer not found: id=%d".formatted(UNKNOWN_ID)))
                .when(manufacturerService)
                .delete(UNKNOWN_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(manufacturerService).delete(UNKNOWN_ID);
    }

    @Test
    @DisplayName("delete: database constraint violation -> returns 409 Conflict with error details")
    void delete_databaseConstraintViolation_returnsConflict() throws Exception {
        doThrow(new DataIntegrityViolationException(
                "Foreign key constraint violation"))
                .when(manufacturerService)
                .delete(TOYOTA_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", TOYOTA_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Operation violates a database constraint"));

        verify(manufacturerService).delete(TOYOTA_ID);
    }
}
