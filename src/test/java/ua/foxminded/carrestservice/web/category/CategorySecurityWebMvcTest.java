package ua.foxminded.carrestservice.web.category;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.category.CategoryControllerTestConstants.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ua.foxminded.carrestservice.security.SecurityConfig;
import ua.foxminded.carrestservice.service.category.CategoryService;
import ua.foxminded.carrestservice.service.category.dto.CategoryRequest;
import ua.foxminded.carrestservice.testconfig.KeycloakTestConfig;
import ua.foxminded.carrestservice.testconfig.KeycloakTestTokens;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, KeycloakTestConfig.class})
class CategorySecurityWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KeycloakTestTokens tokens;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @DisplayName("create: missing JWT -> returns 401 without calling service")
    void create_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Sports Car"}
                        """))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(categoryService);
    }

    @Test
    @DisplayName("create: JWT with cars:write -> returns 403")
    void create_withCarsScope_returnsForbidden() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.carEditorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Sports Car"}
                        """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(categoryService);
    }

    @Test
    @DisplayName("create: JWT without write scopes -> returns 403")
    void create_withViewerToken_returnsForbidden() throws Exception {
        mockMvc.perform(post(BASE_URL)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.viewerToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Sports Car"}
                        """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(categoryService);
    }

    @Test
    @DisplayName("create: JWT with catalog:write -> reaches controller")
    void create_withCatalogScope_returnsCreated() throws Exception {
        when(categoryService.create(any(CategoryRequest.class)))
                .thenReturn(SEDAN_ID);

        mockMvc.perform(post(BASE_URL)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.catalogEditorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Sports Car"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", BASE_URL + "/" + SEDAN_ID))
                .andExpect(jsonPath("$.id").value(SEDAN_ID));

        verify(categoryService)
                .create(new CategoryRequest(SPORTS_CAR_NAME));
    }
}
