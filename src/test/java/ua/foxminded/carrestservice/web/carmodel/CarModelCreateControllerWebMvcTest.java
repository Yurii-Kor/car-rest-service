
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
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.service.carmodel.CarModelService;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelRequest;

@WebMvcTest(CarModelController.class)
@ActiveProfiles("test")
class CarModelCreateControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarModelService carModelService;

    @Test
    @DisplayName("create: valid JSON with extra spaces -> returns 201 Created and Location header")
    void create_validRequest_returnsCreatedAndLocation() throws Exception {
        when(carModelService.create(
                eq(TOYOTA_ID), any(CarModelRequest.class)))
                .thenReturn(COROLLA_ID);

        mockMvc.perform(post(MANUFACTURER_MODELS_URL, TOYOTA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "  Corolla   Cross  "}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", MODELS_URL + "/" + COROLLA_ID))
                .andExpect(content().string(""));

        verify(carModelService).create(
                TOYOTA_ID,
                new CarModelRequest("Corolla Cross"));
    }

    @Test
    @DisplayName("create: blank name -> returns 400 Bad Request without invoking service")
    void create_blankName_returnsBadRequest() throws Exception {
        mockMvc.perform(post(MANUFACTURER_MODELS_URL, TOYOTA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "      "}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.errors[0]").exists());

        verifyNoInteractions(carModelService);
    }

    @Test
    @DisplayName("create: duplicate model name -> returns 409 Conflict with error details")
    void create_duplicateName_returnsConflict() throws Exception {
        when(carModelService.create(
                eq(TOYOTA_ID), any(CarModelRequest.class)))
                .thenThrow(new EntityExistsException(
                        "Car model already exists: manufacturerId=101, name=Corolla"));

        mockMvc.perform(post(MANUFACTURER_MODELS_URL, TOYOTA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Corolla"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Car model already exists: manufacturerId=101, name=Corolla"));

        verify(carModelService).create(
                TOYOTA_ID,
                new CarModelRequest(COROLLA_NAME));
    }

    @Test
    @DisplayName("create: unknown manufacturer -> returns 404 Not Found")
    void create_unknownManufacturer_returnsNotFound() throws Exception {
        when(carModelService.create(
                eq(UNKNOWN_MANUFACTURER_ID), any(CarModelRequest.class)))
                .thenThrow(new EntityNotFoundException(
                        "Manufacturer not found: id=%d"
                                .formatted(UNKNOWN_MANUFACTURER_ID)));

        mockMvc.perform(post(
                MANUFACTURER_MODELS_URL, UNKNOWN_MANUFACTURER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Corolla"}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(carModelService).create(
                UNKNOWN_MANUFACTURER_ID,
                new CarModelRequest(COROLLA_NAME));
    }
}
