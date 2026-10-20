
package ua.foxminded.carrestservice.web.category;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.category.CategoryControllerTestConstants.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.json.JsonMapper;
import ua.foxminded.carrestservice.repository.CategoryRepository;
import ua.foxminded.carrestservice.testconfig.PostgreSQLTestConfig;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(PostgreSQLTestConfig.class)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(
        scripts = "/sql/category/category-service-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "/sql/cleanup-database.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class CategoryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("create: valid request -> persists category and returns generated ID")
    void create_validRequest_persistsCategoryAndReturnsId() throws Exception {
        var initialCount = categoryRepository.count();

        var result = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "  Sports    Car  "}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        var response = jsonMapper.readValue(
                result.getResponse().getContentAsString(),
                IdResponse.class);

        var id = response.id();

        assertThat(id).isPositive();

        assertThat(result.getResponse().getHeader("Location"))
                .isEqualTo(BASE_URL + "/" + id);

        var saved = categoryRepository.findById(id).orElseThrow();

        assertThat(saved.getName()).isEqualTo(SPORTS_CAR_NAME);
        assertThat(categoryRepository.existsByName(SPORTS_CAR_NAME))
                .isTrue();
        assertThat(categoryRepository.count())
                .isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("findById: persisted category -> returns correct data")
    void findById_existingCategory_returnsPersistedData() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", SEDAN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SEDAN_ID))
                .andExpect(jsonPath("$.name").value(SEDAN_NAME));
    }

    @Test
    @DisplayName("findById: unknown category -> returns 404 Not Found")
    void findById_unknownCategory_returnsNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("findAll: default pagination -> returns sorted categories from database")
    void findAll_withoutParameters_returnsSortedCategories() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()")
                        .value(INITIAL_CATEGORIES_COUNT))
                .andExpect(jsonPath("$.content[0].name")
                        .value(COUPE_NAME))
                .andExpect(jsonPath("$.content[1].name")
                        .value(HATCHBACK_NAME))
                .andExpect(jsonPath("$.content[2].name")
                        .value(SEDAN_NAME))
                .andExpect(jsonPath("$.content[3].name")
                        .value(TRUCK_NAME))
                .andExpect(jsonPath("$.page.size").value(12))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements")
                        .value(INITIAL_CATEGORIES_COUNT))
                .andExpect(jsonPath("$.page.totalPages").value(1));
    }

    @Test
    @DisplayName("findAll: custom pagination and sorting -> returns correct page")
    void findAll_withPagination_returnsRequestedPage() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("page", "1")
                .param("size", "2")
                .param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id")
                        .value(HATCHBACK_ID))
                .andExpect(jsonPath("$.content[0].name")
                        .value(HATCHBACK_NAME))
                .andExpect(jsonPath("$.content[1].id")
                        .value(COUPE_ID))
                .andExpect(jsonPath("$.content[1].name")
                        .value(COUPE_NAME))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.totalElements")
                        .value(INITIAL_CATEGORIES_COUNT))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    @DisplayName("findAllForSelection: categories -> returns sorted options from database")
    void findAllForSelection_returnsPersistedCategories() throws Exception {
        mockMvc.perform(get(BASE_URL + "/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(INITIAL_CATEGORIES_COUNT))
                .andExpect(jsonPath("$[0].id").value(COUPE_ID))
                .andExpect(jsonPath("$[0].name").value(COUPE_NAME))
                .andExpect(jsonPath("$[1].id").value(HATCHBACK_ID))
                .andExpect(jsonPath("$[1].name").value(HATCHBACK_NAME))
                .andExpect(jsonPath("$[2].id").value(SEDAN_ID))
                .andExpect(jsonPath("$[2].name").value(SEDAN_NAME))
                .andExpect(jsonPath("$[3].id").value(TRUCK_ID))
                .andExpect(jsonPath("$[3].name").value(TRUCK_NAME));
    }

    @Test
    @DisplayName("update: valid request -> persists normalized category name")
    void update_validRequest_persistsNewName() throws Exception {
        mockMvc.perform(put(BASE_URL + "/{id}", HATCHBACK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "  Sports    Car  "}
                        """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        var updated = categoryRepository.findById(HATCHBACK_ID)
                .orElseThrow();

        assertThat(updated.getName()).isEqualTo(SPORTS_CAR_NAME);
        assertThat(categoryRepository.existsByName(SPORTS_CAR_NAME))
                .isTrue();

        mockMvc.perform(get(BASE_URL + "/{id}", HATCHBACK_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(HATCHBACK_ID))
                .andExpect(jsonPath("$.name").value(SPORTS_CAR_NAME));
    }

    @Test
    @DisplayName("delete: existing category -> removes category from database")
    void delete_existingCategory_removesCategory() throws Exception {
        var initialCount = categoryRepository.count();

        mockMvc.perform(delete(BASE_URL + "/{id}", HATCHBACK_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertThat(categoryRepository.existsById(HATCHBACK_ID))
                .isFalse();

        assertThat(categoryRepository.count())
                .isEqualTo(initialCount - 1);

        assertThat(categoryRepository.existsById(SEDAN_ID))
                .isTrue();

        mockMvc.perform(get(BASE_URL + "/{id}", HATCHBACK_ID))
                .andExpect(status().isNotFound());
    }
}
