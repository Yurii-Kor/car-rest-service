
package ua.foxminded.carrestservice.web.category;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.category.CategoryControllerTestConstants.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.service.category.CategoryService;
import ua.foxminded.carrestservice.service.exception.EntityInUseException;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
class CategoryDeleteControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @DisplayName("delete: existing category -> returns 204 No Content")
    void delete_existingCategory_returnsNoContent() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/{id}", SEDAN_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(categoryService).delete(SEDAN_ID);
    }

    @Test
    @DisplayName("delete: unknown category -> returns 404 Not Found")
    void delete_unknownCategory_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException(
                "Category not found: id=%d".formatted(UNKNOWN_ID)))
                .when(categoryService)
                .delete(UNKNOWN_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(categoryService).delete(UNKNOWN_ID);
    }

    @Test
    @DisplayName("delete: category with cars -> returns 409 Conflict")
    void delete_categoryWithCars_returnsConflict() throws Exception {
        var message = "Cannot delete category with existing cars: id=%d"
                .formatted(SEDAN_ID);

        doThrow(new EntityInUseException(message))
                .when(categoryService)
                .delete(SEDAN_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", SEDAN_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value(message));

        verify(categoryService).delete(SEDAN_ID);
    }

    @Test
    @DisplayName("delete: database constraint violation -> returns 409 Conflict with error details")
    void delete_databaseConstraintViolation_returnsConflict() throws Exception {
        doThrow(new DataIntegrityViolationException(
                "Database constraint violation"))
                .when(categoryService)
                .delete(SEDAN_ID);

        mockMvc.perform(delete(BASE_URL + "/{id}", SEDAN_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Operation violates a database constraint"));

        verify(categoryService).delete(SEDAN_ID);
    }
}
