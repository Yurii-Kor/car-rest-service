package ua.foxminded.carrestservice.web.car;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.car.CarControllerTestConstants.*;
import static ua.foxminded.carrestservice.web.car.CarControllerTestFactory.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.service.car.CarService;
import ua.foxminded.carrestservice.service.car.dto.CarDetails;

@WebMvcTest(CarController.class)
@ActiveProfiles("test")
class CarFindControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarService carService;

    @Test
    @DisplayName("findById: existing car -> returns 200 OK with car details")
    void findById_existingCar_returnsOk() throws Exception {
        when(carService.findById(CAR_ID))
                .thenReturn(carDetails(
                        CAR_ID,
                        CAR_YEAR,
                        MODEL_ID,
                        MANUFACTURER_ID,
                        SUV_ID,
                        SEDAN_ID));

        mockMvc.perform(get(BASE_URL + "/{id}", CAR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CAR_ID))
                .andExpect(jsonPath("$.year").value(2020))
                .andExpect(jsonPath("$.modelId").value(MODEL_ID))
                .andExpect(jsonPath("$.manufacturerId")
                        .value(MANUFACTURER_ID))
                .andExpect(jsonPath("$.categoryIds.length()").value(2))
                .andExpect(jsonPath("$.categoryIds[0]").value(SEDAN_ID))
                .andExpect(jsonPath("$.categoryIds[1]").value(SUV_ID));

        verify(carService).findById(CAR_ID);
    }

    @Test
    @DisplayName("findById: unknown car -> returns 404 Not Found")
    void findById_unknownCar_returnsNotFound() throws Exception {
        when(carService.findById(UNKNOWN_CAR_ID))
                .thenThrow(new EntityNotFoundException(
                        "Car not found: id=%d".formatted(UNKNOWN_CAR_ID)));

        mockMvc.perform(get(BASE_URL + "/{id}", UNKNOWN_CAR_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(carService).findById(UNKNOWN_CAR_ID);
    }

    @Test
    @DisplayName("findAll: no pagination parameters -> uses default page size and sorting")
    void findAll_withoutParameters_usesDefaultPagination()
            throws Exception {

        var pageable = PageRequest.of(0, 12, Sort.by("id"));

        var cars = List.of(
                carDetails(
                        CAR_ID, CAR_YEAR, MODEL_ID,
                        MANUFACTURER_ID, SEDAN_ID),
                carDetails(
                        SECOND_CAR_ID, UPDATED_YEAR, SECOND_MODEL_ID,
                        MANUFACTURER_ID, SUV_ID));

        when(carService.findAll(pageable))
                .thenReturn(new PageImpl<>(cars, pageable, 2));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(CAR_ID))
                .andExpect(jsonPath("$.content[0].year").value(2020))
                .andExpect(jsonPath("$.content[1].id")
                        .value(SECOND_CAR_ID))
                .andExpect(jsonPath("$.totalElements").value(2));

        verify(carService).findAll(pageable);
    }

    @Test
    @DisplayName("findAll: custom pagination and sorting -> returns requested page")
    void findAll_customPagination_returnsRequestedPage()
            throws Exception {

        var pageable = PageRequest.of(
                1, 2, Sort.by(Sort.Direction.DESC, "id"));

        var page = new PageImpl<>(
                List.of(carDetails(
                        CAR_ID, CAR_YEAR, MODEL_ID,
                        MANUFACTURER_ID, SEDAN_ID)),
                pageable,
                3);

        when(carService.findAll(pageable)).thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                .param("page", "1")
                .param("size", "2")
                .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(CAR_ID))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.totalPages").value(2));

        verify(carService).findAll(pageable);
    }

    @Test
    @DisplayName("findAll: no cars -> returns empty page")
    void findAll_noCars_returnsEmptyPage() throws Exception {
        var pageable = PageRequest.of(0, 12, Sort.by("id"));

        when(carService.findAll(pageable))
                .thenReturn(new PageImpl<CarDetails>(
                        List.of(), pageable, 0));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));

        verify(carService).findAll(pageable);
    }
}
