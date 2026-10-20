package ua.foxminded.carrestservice.web.manufacturer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.manufacturer.ManufacturerControllerTestConstants.*;

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
import ua.foxminded.carrestservice.service.exception.EntityInUseException;
import ua.foxminded.carrestservice.service.manufacturer.ManufacturerService;
import ua.foxminded.carrestservice.service.manufacturer.dto.ManufacturerRequest;

@WebMvcTest(ManufacturerController.class)
@ActiveProfiles("test")
class ManufacturerControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManufacturerService manufacturerService;

    @Test
    @DisplayName("create: duplicate name -> returns 409 Conflict with error details")
    void create_duplicateName_returnsConflict() throws Exception {
        when(manufacturerService.create(any(ManufacturerRequest.class)))
                .thenThrow(new EntityExistsException(
                        "Manufacturer already exists: Toyota"));

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Toyota"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Manufacturer already exists: Toyota"));

        verify(manufacturerService)
                .create(new ManufacturerRequest(TOYOTA_NAME));
    }

    @Test
    @DisplayName("update: unknown manufacturer -> returns 404 Not Found")
    void update_unknownManufacturer_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException(
                "Manufacturer not found: id=%d".formatted(UNKNOWN_ID)))
                .when(manufacturerService)
                .update(eq(UNKNOWN_ID), any(ManufacturerRequest.class));

        mockMvc.perform(put(BASE_URL + "/{id}", UNKNOWN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Toyota"}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(manufacturerService).update(
                UNKNOWN_ID,
                new ManufacturerRequest(TOYOTA_NAME));
    }

    @Test
    @DisplayName("update: duplicate name -> returns 409 Conflict with error details")
    void update_duplicateName_returnsConflict() throws Exception {
        doThrow(new EntityExistsException(
                "Manufacturer already exists: BMW"))
                .when(manufacturerService)
                .update(eq(TOYOTA_ID), any(ManufacturerRequest.class));

        mockMvc.perform(put(BASE_URL + "/{id}", TOYOTA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "BMW"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Manufacturer already exists: BMW"));

        verify(manufacturerService).update(
                TOYOTA_ID,
                new ManufacturerRequest(BMW_NAME));
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
    @DisplayName("delete: database constraint violation -> returns 409 Conflict")
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

    @Test
    @DisplayName("delete: manufacturer with cars -> returns 409 Conflict")
    void delete_manufacturerWithCars_returnsConflict() throws Exception {
        var message = "Cannot delete manufacturer with existing cars: id=%d"
                .formatted(TOYOTA_ID);

        doThrow(new EntityInUseException(message))
                .when(manufacturerService)
                .delete(TOYOTA_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", TOYOTA_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value(message));

        verify(manufacturerService).delete(TOYOTA_ID);
    }
}
