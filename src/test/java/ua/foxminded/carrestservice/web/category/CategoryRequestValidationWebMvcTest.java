package ua.foxminded.carrestservice.web.category;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.category.CategoryControllerTestConstants.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import ua.foxminded.carrestservice.service.category.CategoryService;
import ua.foxminded.carrestservice.service.category.dto.CategoryRequest;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
class CategoryRequestValidationWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("validCases")
    @DisplayName("create: valid category name -> returns 201 Created")
    void create_validRequest_returnsCreated(
            String scenario,
            String json,
            String expectedName) throws Exception {

        when(categoryService.create(any(CategoryRequest.class)))
                .thenReturn(SEDAN_ID);

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Location", BASE_URL + "/" + SEDAN_ID))
                .andExpect(jsonPath("$.id").value(SEDAN_ID));

        verify(categoryService).create(
                new CategoryRequest(expectedName));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("validCases")
    @DisplayName("update: valid category name -> returns 204 No Content")
    void update_validRequest_returnsNoContent(
            String scenario,
            String json,
            String expectedName) throws Exception {

        mockMvc.perform(put(BASE_URL + "/{id}", SEDAN_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(categoryService).update(
                SEDAN_ID,
                new CategoryRequest(expectedName));
    }

    static Stream<Arguments> validCases() {
        return Stream.of(
                Arguments.of(
                        "one character",
                        requestJson("\"A\""),
                        "A"),
    
                Arguments.of(
                        "maximum length of 255",
                        requestJson("\"" + "A".repeat(255) + "\""),
                        "A".repeat(255)),
    
                Arguments.of(
                        "tabs and spaces -> normalized name",
                        requestJson("\"  Sports\\t  Car  \""),
                        SPORTS_CAR_NAME));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("invalidCases")
    @DisplayName("create: invalid category name -> returns 400 Bad Request")
    void create_invalidRequest_returnsBadRequest(
            String scenario,
            String json,
            String expectedMessage) throws Exception {

        assertValidationError(
                post(BASE_URL),
                json,
                expectedMessage);

        verifyNoInteractions(categoryService);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("invalidCases")
    @DisplayName("update: invalid category name -> returns 400 Bad Request")
    void update_invalidRequest_returnsBadRequest(
            String scenario,
            String json,
            String expectedMessage) throws Exception {

        assertValidationError(
                put(BASE_URL + "/{id}", SEDAN_ID),
                json,
                expectedMessage);

        verifyNoInteractions(categoryService);
    }

    static Stream<Arguments> invalidCases() {
        return Stream.of(
                Arguments.of(
                        "null name",
                        requestJson("null"),
                        "must not be blank"),

                Arguments.of(
                        "empty name",
                        requestJson("\"\""),
                        "must not be blank"),

                Arguments.of(
                        "blank name",
                        requestJson("\"   \\t   \""),
                        "must not be blank"),

                Arguments.of(
                        "length exceeds 255",
                        requestJson("\"" + "A".repeat(256) + "\""),
                        "size must be between 0 and 255"),

                Arguments.of(
                        "missing name",
                        "{}",
                        "must not be blank"));
    }

    private void assertValidationError(
            MockHttpServletRequestBuilder request,
            String json,
            String expectedMessage) throws Exception {

        mockMvc.perform(request
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.errors", hasItem(allOf(
                        containsString("name"),
                        containsString(expectedMessage)))));
    }

    private static String requestJson(String name) {
        return """
                {
                    "name": %s
                }
                """.formatted(name);
    }
}
