
package ua.foxminded.carrestservice.web.category;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.category.CategoryControllerTestConstants.*;
import static ua.foxminded.carrestservice.web.category.CategoryControllerTestFactory.*;

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
import ua.foxminded.carrestservice.service.category.CategoryService;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
class CategoryFindControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @DisplayName("findById: existing category -> returns 200 OK with category JSON")
    void findById_existingCategory_returnsOk() throws Exception {
        when(categoryService.findById(SEDAN_ID))
                .thenReturn(category(SEDAN_ID, SEDAN_NAME));

        mockMvc.perform(get(BASE_URL + "/{id}", SEDAN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SEDAN_ID))
                .andExpect(jsonPath("$.name").value(SEDAN_NAME));
    }

    @Test
    @DisplayName("findById: unknown category -> returns 404 Not Found")
    void findById_unknownCategory_returnsNotFound() throws Exception {
        when(categoryService.findById(UNKNOWN_ID))
                .thenThrow(new EntityNotFoundException(
                        "Category not found: id=%d".formatted(UNKNOWN_ID)));

        mockMvc.perform(get(BASE_URL + "/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("findAll: no pagination parameters -> uses default page size and sorting")
    void findAll_withoutParameters_usesDefaultPagination() throws Exception {
        var pageable = PageRequest.of(0, 12, Sort.by("name"));

        var categories = List.of(
                category(SEDAN_ID, SEDAN_NAME),
                category(SUV_ID, SUV_NAME));

        when(categoryService.findAll(pageable))
                .thenReturn(new PageImpl<>(
                        categories, pageable, categories.size()));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page.size").value(12))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(1));

        verify(categoryService).findAll(pageable);
    }

    @Test
    @DisplayName("findAll: custom pagination and sorting -> returns requested page with metadata")
    void findAll_withPagination_returnsRequestedPage() throws Exception {
        var pageable = PageRequest.of(
                1, 2, Sort.by(Sort.Direction.DESC, "name"));

        var page = new PageImpl<>(
                List.of(category(SEDAN_ID, SEDAN_NAME)),
                pageable,
                3);

        when(categoryService.findAll(pageable))
                .thenReturn(page);

        mockMvc.perform(get(BASE_URL)
                .param("page", "1")
                .param("size", "2")
                .param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(SEDAN_ID))
                .andExpect(jsonPath("$.content[0].name").value(SEDAN_NAME))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(3))
                .andExpect(jsonPath("$.page.totalPages").value(2));

        verify(categoryService).findAll(pageable);
    }

    @Test
    @DisplayName("findAllForSelection: categories exist -> returns 200 OK with ID and name options")
    void findAllForSelection_returnsCategoryOptions() throws Exception {
        when(categoryService.findAllForSelection())
                .thenReturn(List.of(
                        category(SEDAN_ID, SEDAN_NAME),
                        category(SUV_ID, SUV_NAME)));

        mockMvc.perform(get(BASE_URL + "/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(SEDAN_ID))
                .andExpect(jsonPath("$[0].name").value(SEDAN_NAME))
                .andExpect(jsonPath("$[1].id").value(SUV_ID))
                .andExpect(jsonPath("$[1].name").value(SUV_NAME));

        verify(categoryService).findAllForSelection();
    }
}
