
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
class CarModelUpdateControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarModelService carModelService;

    @Test
    @DisplayName("update: valid JSON with extra spaces -> normalizes name and returns 204 No Content")
    void update_validRequest_returnsNoContent() throws Exception {
        mockMvc.perform(put(MODELS_URL + "/{id}", COROLLA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "  Corolla   Cross  "}
                        """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(carModelService).update(
                COROLLA_ID,
                new CarModelRequest("Corolla Cross"));
    }

    @Test
    @DisplayName("update: blank name -> returns 400 Bad Request without invoking service")
    void update_blankName_returnsBadRequest() throws Exception {
        mockMvc.perform(put(MODELS_URL + "/{id}", COROLLA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "      "}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0]").exists());

        verifyNoInteractions(carModelService);
    }

    @Test
    @DisplayName("update: name exceeds 255 characters -> returns 400 Bad Request")
    void update_nameTooLong_returnsBadRequest() throws Exception {
        var json = """
                {"name": "%s"}
                """.formatted("A".repeat(256));

        mockMvc.perform(put(MODELS_URL + "/{id}", COROLLA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(carModelService);
    }

    @Test
    @DisplayName("update: unknown model -> returns 404 Not Found")
    void update_unknownModel_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException(
                "Car model not found: id=%d".formatted(UNKNOWN_MODEL_ID)))
                .when(carModelService)
                .update(eq(UNKNOWN_MODEL_ID), any(CarModelRequest.class));

        mockMvc.perform(put(MODELS_URL + "/{id}", UNKNOWN_MODEL_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Corolla"}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(carModelService).update(
                UNKNOWN_MODEL_ID,
                new CarModelRequest(COROLLA_NAME));
    }

    @Test
    @DisplayName("update: duplicate model name -> returns 409 Conflict with error details")
    void update_duplicateName_returnsConflict() throws Exception {
        doThrow(new EntityExistsException(
                "Car model already exists: manufacturerId=101, name=Camry"))
                .when(carModelService)
                .update(eq(COROLLA_ID), any(CarModelRequest.class));

        mockMvc.perform(put(MODELS_URL + "/{id}", COROLLA_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Camry"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Car model already exists: manufacturerId=101, name=Camry"));

        verify(carModelService).update(
                COROLLA_ID,
                new CarModelRequest(CAMRY_NAME));
    }
}
