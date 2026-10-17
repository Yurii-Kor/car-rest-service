package ua.foxminded.carrestservice.web.manufacturer;

import static org.mockito.ArgumentMatchers.any;
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
import ua.foxminded.carrestservice.service.manufacturer.ManufacturerService;
import ua.foxminded.carrestservice.service.manufacturer.dto.ManufacturerRequest;

@WebMvcTest(ManufacturerController.class)
@ActiveProfiles("test")
class ManufacturerCreateControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManufacturerService manufacturerService;

    @Test
    @DisplayName("create: valid JSON with extra spaces -> returns 201 Created and Location header")
    void create_validRequest_returnsCreatedAndLocation() throws Exception {
        when(manufacturerService.create(any(ManufacturerRequest.class)))
                .thenReturn(65L);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "  Land   Rover  "}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", BASE_URL + "/65"))
                .andExpect(content().string(""));

        verify(manufacturerService)
                .create(new ManufacturerRequest("Land Rover"));
    }

    @Test
    @DisplayName("create: blank name -> returns 400 Bad Request without invoking service")
    void create_blankName_returnsBadRequest() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "      "}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.errors[0]").exists());

        verifyNoInteractions(manufacturerService);
    }

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
    }
}
