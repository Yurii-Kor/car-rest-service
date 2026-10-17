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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.service.manufacturer.ManufacturerService;
import ua.foxminded.carrestservice.service.manufacturer.dto.ManufacturerRequest;

@WebMvcTest(ManufacturerController.class)
@ActiveProfiles("test")
class ManufacturerUpdateControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManufacturerService manufacturerService;

    @Test
    @DisplayName("update: valid JSON with extra spaces -> normalizes name and returns 204 No Content")
    void update_validRequest_returnsNoContent() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", TOYOTA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "  Land   Rover  "}
                        """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(manufacturerService).update(
                TOYOTA_ID,
                new ManufacturerRequest("Land Rover"));
    }

    @Test
    @DisplayName("update: blank name -> returns 400 Bad Request without invoking service")
    void update_blankName_returnsBadRequest() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", TOYOTA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "      "}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0]").exists());

        verifyNoInteractions(manufacturerService);
    }

    @Test
    @DisplayName("update: name exceeds 255 characters -> returns 400 Bad Request")
    void update_nameTooLong_returnsBadRequest() throws Exception {
        var json = """
                {"name": "%s"}
                """.formatted("A".repeat(256));

        mockMvc.perform(put(BASE_URL + "/{id}", TOYOTA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(manufacturerService);
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
}
