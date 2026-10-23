package ua.foxminded.carrestservice.web.manufacturer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.manufacturer.ManufacturerControllerTestConstants.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.json.JsonMapper;
import ua.foxminded.carrestservice.repository.ManufacturerRepository;
import ua.foxminded.carrestservice.testconfig.KeycloakTestConfig;
import ua.foxminded.carrestservice.testconfig.KeycloakTestTokens;
import ua.foxminded.carrestservice.testconfig.PostgreSQLTestConfig;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({PostgreSQLTestConfig.class, KeycloakTestConfig.class})
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(
        scripts = "/sql/manufacturer/manufacturer-service-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "/sql/cleanup-database.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class ManufacturerControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private KeycloakTestTokens tokens;

    @Test
    @DisplayName("create: valid request -> persists manufacturer and returns generated ID")
    void create_validRequest_persistsManufacturerAndReturnsId()
            throws Exception {

        var initialCount = manufacturerRepository.count();

        var result = mockMvc.perform(post(BASE_URL)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.catalogEditorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "  Land   Rover  "}
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

        var saved = manufacturerRepository.findById(id)
                .orElseThrow();

        assertThat(saved.getName()).isEqualTo(LAND_ROVER_NAME);
        assertThat(manufacturerRepository.count())
                .isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("findById: existing manufacturer -> returns persisted data")
    void findById_existingManufacturer_returnsPersistedData()
            throws Exception {

        mockMvc.perform(get(BASE_URL + "/{id}", TOYOTA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TOYOTA_ID))
                .andExpect(jsonPath("$.name").value(TOYOTA_NAME));
    }

    @Test
    @DisplayName("findById: unknown manufacturer -> returns 404 Not Found")
    void findById_unknownManufacturer_returnsNotFound()
            throws Exception {

        mockMvc.perform(get(BASE_URL + "/{id}", UNKNOWN_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("findAll: default pagination -> returns sorted manufacturers from database")
    void findAll_withoutParameters_returnsSortedManufacturers()
            throws Exception {

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()")
                        .value(INITIAL_MANUFACTURERS_COUNT))
                .andExpect(jsonPath("$.content[0].id").value(BMW_ID))
                .andExpect(jsonPath("$.content[0].name").value(BMW_NAME))
                .andExpect(jsonPath("$.content[1].id").value(PROTOTYPE_ID))
                .andExpect(jsonPath("$.content[1].name").value(PROTOTYPE_NAME))
                .andExpect(jsonPath("$.content[2].id").value(TOYOTA_ID))
                .andExpect(jsonPath("$.content[2].name").value(TOYOTA_NAME))
                .andExpect(jsonPath("$.content[3].id").value(UNUSED_ID))
                .andExpect(jsonPath("$.content[3].name").value(UNUSED_NAME))
                .andExpect(jsonPath("$.page.size").value(12))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements")
                        .value(INITIAL_MANUFACTURERS_COUNT))
                .andExpect(jsonPath("$.page.totalPages").value(1));
    }

    @Test
    @DisplayName("findAll: custom pagination and sorting -> returns requested page")
    void findAll_withPagination_returnsRequestedPage()
            throws Exception {

        mockMvc.perform(get(BASE_URL)
                .param("page", "1")
                .param("size", "2")
                .param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(PROTOTYPE_ID))
                .andExpect(jsonPath("$.content[0].name").value(PROTOTYPE_NAME))
                .andExpect(jsonPath("$.content[1].id").value(BMW_ID))
                .andExpect(jsonPath("$.content[1].name").value(BMW_NAME))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.totalElements")
                        .value(INITIAL_MANUFACTURERS_COUNT))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    @DisplayName("findAllForSelection: manufacturers -> returns sorted options from database")
    void findAllForSelection_returnsPersistedManufacturers()
            throws Exception {

        mockMvc.perform(get(BASE_URL + "/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(INITIAL_MANUFACTURERS_COUNT))
                .andExpect(jsonPath("$[0].id").value(BMW_ID))
                .andExpect(jsonPath("$[0].name").value(BMW_NAME))
                .andExpect(jsonPath("$[1].id").value(PROTOTYPE_ID))
                .andExpect(jsonPath("$[1].name").value(PROTOTYPE_NAME))
                .andExpect(jsonPath("$[2].id").value(TOYOTA_ID))
                .andExpect(jsonPath("$[2].name").value(TOYOTA_NAME))
                .andExpect(jsonPath("$[3].id").value(UNUSED_ID))
                .andExpect(jsonPath("$[3].name").value(UNUSED_NAME));
    }

    @Test
    @DisplayName("update: valid request -> persists normalized manufacturer name")
    void update_validRequest_persistsNewName()
            throws Exception {

        mockMvc.perform(put(BASE_URL + "/{id}", TOYOTA_ID)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.catalogEditorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "  Toyota   Motors  "}
                        """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        var updated = manufacturerRepository.findById(TOYOTA_ID)
                .orElseThrow();

        assertThat(updated.getName()).isEqualTo(TOYOTA_UPDATED_NAME);

        mockMvc.perform(get(BASE_URL + "/{id}", TOYOTA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(TOYOTA_ID))
                .andExpect(jsonPath("$.name").value(TOYOTA_UPDATED_NAME));
    }

    @Test
    @DisplayName("delete: unused manufacturer -> removes entity from database")
    void delete_unusedManufacturer_removesManufacturer()
            throws Exception {

        var initialCount = manufacturerRepository.count();

        mockMvc.perform(delete(BASE_URL + "/{id}", UNUSED_ID)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.catalogEditorToken()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertThat(manufacturerRepository.existsById(UNUSED_ID))
                .isFalse();

        assertThat(manufacturerRepository.count())
                .isEqualTo(initialCount - 1);

        assertThat(manufacturerRepository.existsById(TOYOTA_ID))
                .isTrue();

        mockMvc.perform(get(BASE_URL + "/{id}", UNUSED_ID))
                .andExpect(status().isNotFound());
    }
}
