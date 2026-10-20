package ua.foxminded.carrestservice.web.carmodel;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.carmodel.CarModelControllerTestConstants.*;
import static ua.foxminded.carrestservice.web.carmodel.CarModelControllerTestFactory.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.service.carmodel.CarModelService;

@WebMvcTest(CarModelController.class)
@ActiveProfiles("test")
class CarModelFindControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarModelService carModelService;

    @Test
    @DisplayName("findById: existing model -> returns 200 OK with model and manufacturer IDs")
    void findById_existingModel_returnsOk() throws Exception {
        when(carModelService.findById(COROLLA_ID))
                .thenReturn(carModel(
                        COROLLA_ID, COROLLA_NAME, TOYOTA_ID, TOYOTA_NAME));

        mockMvc.perform(get(MODELS_URL + "/{id}", COROLLA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(COROLLA_ID))
                .andExpect(jsonPath("$.name").value(COROLLA_NAME))
                .andExpect(jsonPath("$.manufacturerId").value(TOYOTA_ID));
    }

    @Test
    @DisplayName("findById: unknown model -> returns 404 Not Found")
    void findById_unknownModel_returnsNotFound() throws Exception {
        when(carModelService.findById(UNKNOWN_MODEL_ID))
                .thenThrow(new EntityNotFoundException(
                        "Car model not found: id=%d"
                                .formatted(UNKNOWN_MODEL_ID)));

        mockMvc.perform(get(MODELS_URL + "/{id}", UNKNOWN_MODEL_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("findAllByManufacturerId: no pagination parameters -> uses default page size and sorting")
    void findAllByManufacturerId_withoutParameters_usesDefaultPagination()
            throws Exception {

        var pageable = PageRequest.of(0, 12, Sort.by("name"));

        var models = List.of(
                carModel(CAMRY_ID, CAMRY_NAME, TOYOTA_ID, TOYOTA_NAME),
                carModel(COROLLA_ID, COROLLA_NAME, TOYOTA_ID, TOYOTA_NAME));

        when(carModelService.findAllByManufacturerId(TOYOTA_ID, pageable))
                .thenReturn(new PageImpl<>(
                        models, pageable, models.size()));

        mockMvc.perform(get(MODELS_URL)
                .param("manufacturerId", TOYOTA_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value(CAMRY_NAME))
                .andExpect(jsonPath("$.content[1].name").value(COROLLA_NAME))
                .andExpect(jsonPath("$.page.size").value(12))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(1));

        verify(carModelService).findAllByManufacturerId(TOYOTA_ID, pageable);
    }

    @Test
    @DisplayName("findAllByManufacturerId: custom pagination and sorting -> returns requested page with metadata")
    void findAllByManufacturerId_withPagination_returnsRequestedPage()
            throws Exception {

        var pageable = PageRequest.of(
                1, 2, Sort.by(Sort.Direction.DESC, "name"));

        var page = new PageImpl<>(
                List.of(carModel(
                        CAMRY_ID, CAMRY_NAME, TOYOTA_ID, TOYOTA_NAME)),
                pageable,
                3);

        when(carModelService.findAllByManufacturerId(TOYOTA_ID, pageable))
                .thenReturn(page);

        mockMvc.perform(get(MODELS_URL)
                .param("manufacturerId", TOYOTA_ID.toString())
                .param("page", "1")
                .param("size", "2")
                .param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(CAMRY_ID))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2));

        verify(carModelService).findAllByManufacturerId(TOYOTA_ID, pageable);
    }

    @Test
    @DisplayName("findAllForSelection: manufacturer has models -> returns 200 OK with sorted model options")
    void findAllForSelection_returnsModelOptions() throws Exception {
        when(carModelService.findAllForSelection(TOYOTA_ID))
                .thenReturn(List.of(
                        carModel(CAMRY_ID, CAMRY_NAME, TOYOTA_ID, TOYOTA_NAME),
                        carModel(COROLLA_ID, COROLLA_NAME, TOYOTA_ID, TOYOTA_NAME)));

        mockMvc.perform(get(MODELS_URL + "/options")
                .param("manufacturerId", TOYOTA_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(CAMRY_ID))
                .andExpect(jsonPath("$[0].name").value(CAMRY_NAME))
                .andExpect(jsonPath("$[0].manufacturerId").value(TOYOTA_ID))
                .andExpect(jsonPath("$[1].id").value(COROLLA_ID))
                .andExpect(jsonPath("$[1].name").value(COROLLA_NAME));

        verify(carModelService).findAllForSelection(TOYOTA_ID);
    }

    @Test
    @DisplayName("findAllByManufacturerId: unknown manufacturer -> returns 404 Not Found")
    void findAllByManufacturerId_unknownManufacturer_returnsNotFound()
            throws Exception {

        when(carModelService.findAllByManufacturerId(
                eq(UNKNOWN_MANUFACTURER_ID), any(Pageable.class)))
                .thenThrow(new EntityNotFoundException(
                        "Manufacturer not found: id=%d"
                                .formatted(UNKNOWN_MANUFACTURER_ID)));

        mockMvc.perform(get(MODELS_URL)
                .param("manufacturerId",
                        UNKNOWN_MANUFACTURER_ID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
