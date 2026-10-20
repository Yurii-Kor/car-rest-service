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
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelCreateRequest;

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
        when(carModelService.create(any(CarModelCreateRequest.class)))
                .thenReturn(COROLLA_ID);

        mockMvc.perform(post(MODELS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "  Corolla   Cross  ",
                            "manufacturerId": %d
                        }
                        """.formatted(TOYOTA_ID)))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", MODELS_URL + "/" + COROLLA_ID))
                .andExpect(jsonPath("$.id").value(COROLLA_ID));

        verify(carModelService).create(
                new CarModelCreateRequest("Corolla Cross", TOYOTA_ID));
    }

    @Test
    @DisplayName("create: blank name -> returns 400 Bad Request without invoking service")
    void create_blankName_returnsBadRequest() throws Exception {
        mockMvc.perform(post(MODELS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "      ",
                            "manufacturerId": %d
                        }
                        """.formatted(TOYOTA_ID)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.errors[0]").exists());

        verifyNoInteractions(carModelService);
    }

    @Test
    @DisplayName("create: missing manufacturer ID -> returns 400 Bad Request")
    void create_missingManufacturerId_returnsBadRequest()
            throws Exception {

        mockMvc.perform(post(MODELS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Corolla"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.errors[0]")
                        .value(org.hamcrest.Matchers.containsString(
                                "manufacturerId")));

        verifyNoInteractions(carModelService);
    }

    @Test
    @DisplayName("create: non-positive manufacturer ID -> returns 400 Bad Request")
    void create_invalidManufacturerId_returnsBadRequest()
            throws Exception {

        mockMvc.perform(post(MODELS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Corolla",
                            "manufacturerId": 0
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0]")
                        .value(org.hamcrest.Matchers.containsString(
                                "manufacturerId")));

        verifyNoInteractions(carModelService);
    }

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
}
