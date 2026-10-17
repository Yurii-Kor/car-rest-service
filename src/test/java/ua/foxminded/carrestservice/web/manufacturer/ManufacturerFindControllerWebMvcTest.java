package ua.foxminded.carrestservice.web.manufacturer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.manufacturer.ManufacturerControllerTestConstants.*;
import static ua.foxminded.carrestservice.web.manufacturer.ManufacturerControllerTestFactory.*;

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
import ua.foxminded.carrestservice.service.manufacturer.ManufacturerService;

@WebMvcTest(ManufacturerController.class)
@ActiveProfiles("test")
class ManufacturerFindControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ManufacturerService manufacturerService;

    @Test
    @DisplayName("findById: existing manufacturer -> returns 200 OK with manufacturer JSON")
    void findById_existingManufacturer_returnsOk() throws Exception {
        when(manufacturerService.findById(TOYOTA_ID))
                .thenReturn(manufacturer(TOYOTA_ID, TOYOTA_NAME));

        mockMvc.perform(get(BASE_URL + "/{id}", TOYOTA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TOYOTA_ID))
                .andExpect(jsonPath("$.name").value(TOYOTA_NAME));
    }

    @Test
    @DisplayName("findById: unknown manufacturer -> returns 404 Not Found")
    void findById_unknownManufacturer_returnsNotFound() throws Exception {
        when(manufacturerService.findById(UNKNOWN_ID))
                .thenThrow(new EntityNotFoundException(
                        "Manufacturer not found: id=%d"
                                .formatted(UNKNOWN_ID)));

        mockMvc.perform(get(BASE_URL + "/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("findAll: no pagination parameters -> uses default page size and sorting")
    void findAll_withoutParameters_usesDefaultPagination() throws Exception {
        var manufacturers = List.of(
                manufacturer(BMW_ID, BMW_NAME),
                manufacturer(TOYOTA_ID, TOYOTA_NAME));

        when(manufacturerService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(manufacturers));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        verify(manufacturerService).findAll(
                PageRequest.of(0, 12, Sort.by("name")));
    }

    @Test
    @DisplayName("findAll: custom pagination and sorting -> returns requested page with metadata")
    void findAll_withPagination_returnsRequestedPage() throws Exception {
        var pageable = PageRequest.of(
                1, 2, Sort.by(Sort.Direction.DESC, "name"));

        var page = new PageImpl<>(
                List.of(manufacturer(TOYOTA_ID, TOYOTA_NAME)),
                pageable,
                3);

        when(manufacturerService.findAll(pageable))
                .thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                .param("page", "1")
                .param("size", "2")
                .param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(TOYOTA_ID))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.number").value(1));

        verify(manufacturerService).findAll(pageable);
    }

    @Test
    @DisplayName("findAllForSelection: manufacturers exist -> returns 200 OK with ID and name options")
    void findAllForSelection_returnsManufacturerOptions() throws Exception {
        when(manufacturerService.findAllForSelection())
                .thenReturn(List.of(
                        manufacturer(BMW_ID, BMW_NAME),
                        manufacturer(TOYOTA_ID, TOYOTA_NAME)));

        mockMvc.perform(get(BASE_URL + "/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(BMW_ID))
                .andExpect(jsonPath("$[0].name").value(BMW_NAME))
                .andExpect(jsonPath("$[1].id").value(TOYOTA_ID))
                .andExpect(jsonPath("$[1].name").value(TOYOTA_NAME));

        verify(manufacturerService).findAllForSelection();
    }
}
