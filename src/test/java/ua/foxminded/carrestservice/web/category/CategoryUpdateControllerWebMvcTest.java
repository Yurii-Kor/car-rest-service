package ua.foxminded.carrestservice.web.category;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.category.CategoryControllerTestConstants.*;

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
import ua.foxminded.carrestservice.service.category.CategoryService;
import ua.foxminded.carrestservice.service.category.dto.CategoryRequest;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
class CategoryUpdateControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @DisplayName("update: valid JSON with extra spaces -> normalizes name and returns 204 No Content")
    void update_validRequest_returnsNoContent() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", SEDAN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "  Sports    Car  "}
                        """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(categoryService).update(
                SEDAN_ID,
                new CategoryRequest("Sports Car"));
    }

    @Test
    @DisplayName("update: blank name -> returns 400 Bad Request without invoking service")
    void update_blankName_returnsBadRequest() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", SEDAN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "      "}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0]").exists());

        verifyNoInteractions(categoryService);
    }

    @Test
    @DisplayName("update: name exceeds 255 characters -> returns 400 Bad Request")
    void update_nameTooLong_returnsBadRequest() throws Exception {
        var json = """
                {"name": "%s"}
                """.formatted("A".repeat(256));

        mockMvc.perform(put(BASE_URL + "/{id}", SEDAN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(categoryService);
    }

    @Test
    @DisplayName("update: unknown category -> returns 404 Not Found")
    void update_unknownCategory_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException(
                "Category not found: id=%d".formatted(UNKNOWN_ID)))
                .when(categoryService)
                .update(eq(UNKNOWN_ID), any(CategoryRequest.class));

        mockMvc.perform(put(BASE_URL + "/{id}", UNKNOWN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Sedan"}
                        """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(categoryService).update(
                UNKNOWN_ID,
                new CategoryRequest(SEDAN_NAME));
    }

    @Test
    @DisplayName("update: duplicate category name -> returns 409 Conflict with error details")
    void update_duplicateName_returnsConflict() throws Exception {
        doThrow(new EntityExistsException(
                "Category already exists: SUV"))
                .when(categoryService)
                .update(eq(SEDAN_ID), any(CategoryRequest.class));

        mockMvc.perform(put(BASE_URL + "/{id}", SEDAN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "SUV"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Category already exists: SUV"));

        verify(categoryService).update(
                SEDAN_ID,
                new CategoryRequest(SUV_NAME));
    }
}
