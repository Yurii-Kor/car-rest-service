package ua.foxminded.carrestservice.web.carmodel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static ua.foxminded.carrestservice.web.carmodel.CarModelControllerTestConstants.*;

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
import ua.foxminded.carrestservice.repository.CarModelRepository;
import ua.foxminded.carrestservice.testconfig.PostgreSQLTestConfig;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(PostgreSQLTestConfig.class)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(
        scripts = "/sql/carmodel/car-model-service-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "/sql/cleanup-database.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class CarModelControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("create: valid request -> persists model and returns created ID")
    void create_validRequest_persistsModelAndReturnsId() throws Exception {
        var initialCount = carModelRepository.count();

        var result = mockMvc.perform(post(MODELS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "  %s  ",
                            "manufacturerId": %d
                        }
                        """.formatted(YARIS_NAME, TOYOTA_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        var response = jsonMapper.readValue(
                result.getResponse().getContentAsString(),
                IdResponse.class);

        var id = response.id();

        assertThat(id).isPositive();

        assertThat(result.getResponse().getHeader("Location"))
                .isEqualTo(MODELS_URL + "/" + id);

        var saved = carModelRepository.findById(id).orElseThrow();

        assertThat(saved.getName()).isEqualTo(YARIS_NAME);
        assertThat(carModelRepository.existsByManufacturerIdAndName(
                TOYOTA_ID, YARIS_NAME)).isTrue();
        assertThat(carModelRepository.count())
                .isEqualTo(initialCount + 1);
    }

    @Test
    @DisplayName("findById: persisted model -> returns correct model data")
    void findById_existingModel_returnsPersistedData() throws Exception {
        mockMvc.perform(get(MODELS_URL + "/{id}", COROLLA_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(COROLLA_ID))
                .andExpect(jsonPath("$.name").value(COROLLA_NAME))
                .andExpect(jsonPath("$.manufacturerId").value(TOYOTA_ID));
    }

    @Test
    @DisplayName("findById: unknown model -> returns 404 Not Found")
    void findById_unknownModel_returnsNotFound() throws Exception {
        mockMvc.perform(get(MODELS_URL + "/{id}", UNKNOWN_MODEL_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("findAll: default pagination -> returns sorted persisted models")
    void findAll_existingManufacturer_returnsSortedModels() throws Exception {
        mockMvc.perform(get(MODELS_URL)
                .param("manufacturerId", TOYOTA_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id").value(CAMRY_ID))
                .andExpect(jsonPath("$.content[0].name").value(CAMRY_NAME))
                .andExpect(jsonPath("$.content[1].id").value(COROLLA_ID))
                .andExpect(jsonPath("$.content[1].name").value(COROLLA_NAME))
                .andExpect(jsonPath("$.page.size").value(12))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(1));
    }

    @Test
    @DisplayName("findAll: custom pagination and sorting -> returns requested page")
    void findAll_withPagination_returnsRequestedPage() throws Exception {
        mockMvc.perform(get(MODELS_URL)
                .param("manufacturerId", TOYOTA_ID.toString())
                .param("page", "1")
                .param("size", "1")
                .param("sort", "name,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id").value(CAMRY_ID))
                .andExpect(jsonPath("$.content[0].name").value(CAMRY_NAME))
                .andExpect(jsonPath("$.content[0].manufacturerId")
                        .value(TOYOTA_ID))
                .andExpect(jsonPath("$.page.size").value(1))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(2))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    @DisplayName("findAll: unknown manufacturer -> returns 404 Not Found")
    void findAll_unknownManufacturer_returnsNotFound() throws Exception {
        mockMvc.perform(get(MODELS_URL)
                .param("manufacturerId",
                        UNKNOWN_MANUFACTURER_ID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("findAllForSelection: manufacturer -> returns sorted model options")
    void findAllForSelection_returnsPersistedModels() throws Exception {
        mockMvc.perform(get(MODELS_URL + "/options")
                .param("manufacturerId", TOYOTA_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(CAMRY_ID))
                .andExpect(jsonPath("$[0].name").value(CAMRY_NAME))
                .andExpect(jsonPath("$[0].manufacturerId").value(TOYOTA_ID))
                .andExpect(jsonPath("$[1].id").value(COROLLA_ID))
                .andExpect(jsonPath("$[1].name").value(COROLLA_NAME))
                .andExpect(jsonPath("$[1].manufacturerId").value(TOYOTA_ID));
    }

    @Test
    @DisplayName("update: valid request -> persists normalized name")
    void update_validRequest_persistsNewName() throws Exception {
        mockMvc.perform(put(MODELS_URL + "/{id}", CAMRY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "  Camry   Hybrid  "}
                        """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        var updated = carModelRepository.findById(CAMRY_ID)
                .orElseThrow();

        assertThat(updated.getName()).isEqualTo(CAMRY_UPDATED_NAME);
        assertThat(carModelRepository.existsByManufacturerIdAndName(
                TOYOTA_ID, CAMRY_UPDATED_NAME)).isTrue();

        mockMvc.perform(get(MODELS_URL + "/{id}", CAMRY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CAMRY_ID))
                .andExpect(jsonPath("$.name").value(CAMRY_UPDATED_NAME))
                .andExpect(jsonPath("$.manufacturerId").value(TOYOTA_ID));
    }

    @Test
    @DisplayName("delete: existing model -> removes model from database")
    void delete_existingModel_removesModel() throws Exception {
        var initialCount = carModelRepository.count();

        mockMvc.perform(delete(MODELS_URL + "/{id}", CAMRY_ID))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertThat(carModelRepository.existsById(CAMRY_ID)).isFalse();
        assertThat(carModelRepository.count())
                .isEqualTo(initialCount - 1);

        assertThat(carModelRepository.existsById(COROLLA_ID)).isTrue();

        mockMvc.perform(get(MODELS_URL + "/{id}", CAMRY_ID))
                .andExpect(status().isNotFound());
    }
}
