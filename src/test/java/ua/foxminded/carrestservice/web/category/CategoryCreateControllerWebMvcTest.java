package ua.foxminded.carrestservice.web.category;

import static org.mockito.ArgumentMatchers.any;
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
import ua.foxminded.carrestservice.service.category.CategoryService;
import ua.foxminded.carrestservice.service.category.dto.CategoryRequest;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
class CategoryCreateControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @DisplayName("create: valid JSON with extra spaces -> returns 201 Created and Location header")
    void create_validRequest_returnsCreatedAndLocation() throws Exception {
        when(categoryService.create(any(CategoryRequest.class)))
                .thenReturn(SEDAN_ID);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "  Sports    Car  "}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", BASE_URL + "/" + SEDAN_ID))
                .andExpect(jsonPath("$.id").value(SEDAN_ID));

        verify(categoryService)
                .create(new CategoryRequest("Sports Car"));
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
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.errors[0]").exists());

        verifyNoInteractions(categoryService);
    }

    @Test
    @DisplayName("create: duplicate category name -> returns 409 Conflict with error details")
    void create_duplicateName_returnsConflict() throws Exception {
        when(categoryService.create(any(CategoryRequest.class)))
                .thenThrow(new EntityExistsException(
                        "Category already exists: Sedan"));

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Sedan"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail")
                        .value("Category already exists: Sedan"));

        verify(categoryService)
                .create(new CategoryRequest(SEDAN_NAME));
    }
}
