package ua.foxminded.carrestservice.web.car;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ua.foxminded.carrestservice.web.car.CarControllerTestConstants.*;

import java.time.Year;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.json.JsonMapper;
import ua.foxminded.carrestservice.repository.CarRepository;
import ua.foxminded.carrestservice.testconfig.KeycloakTestConfig;
import ua.foxminded.carrestservice.testconfig.KeycloakTestTokens;
import ua.foxminded.carrestservice.testconfig.PostgreSQLTestConfig;
import ua.foxminded.carrestservice.testutil.CarTestReader;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import({
        PostgreSQLTestConfig.class,
        KeycloakTestConfig.class,
        CarTestReader.class
})
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(
        scripts = "/sql/car/car-service-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "/sql/cleanup-database.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class CarControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CarTestReader carTestReader;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JsonMapper jsonMapper;

    @Autowired
    private KeycloakTestTokens tokens;

    @Test
    @DisplayName("create: valid request -> persists car with category associations")
    void create_validRequest_persistsCarAndReturnsId() throws Exception {
        var initialCount = carRepository.count();

        var result = mockMvc.perform(post(BASE_URL)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.carEditorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": 2023,
                            "modelId": %d,
                            "categoryIds": [%d, %d]
                        }
                        """.formatted(
                                DB_COROLLA_ID,
                                DB_SEDAN_ID,
                                DB_SUV_ID)))
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

        var saved = carTestReader.findById(id);

        assertThat(saved.year()).isEqualTo(Year.of(2023));
        assertThat(saved.modelId()).isEqualTo(DB_COROLLA_ID);
        assertThat(saved.manufacturerId()).isEqualTo(DB_TOYOTA_ID);
        assertThat(saved.categoryIds())
                .containsExactlyInAnyOrder(DB_SEDAN_ID, DB_SUV_ID);

        assertThat(carRepository.count())
                .isEqualTo(initialCount + 1);

        mockMvc.perform(get(BASE_URL + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.year").value(2023))
                .andExpect(jsonPath("$.modelId").value(DB_COROLLA_ID))
                .andExpect(jsonPath("$.manufacturerId").value(DB_TOYOTA_ID))
                .andExpect(jsonPath("$.categoryIds", containsInAnyOrder(
                        Math.toIntExact(DB_SEDAN_ID),
                        Math.toIntExact(DB_SUV_ID))));
    }

    @Test
    @DisplayName("findById: existing car -> returns persisted car details")
    void findById_existingCar_returnsPersistedData() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", DB_EXISTING_CAR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(DB_EXISTING_CAR_ID))
                .andExpect(jsonPath("$.year").value(2020))
                .andExpect(jsonPath("$.modelId").value(DB_COROLLA_ID))
                .andExpect(jsonPath("$.manufacturerId").value(DB_TOYOTA_ID))
                .andExpect(jsonPath("$.categoryIds", containsInAnyOrder(
                        Math.toIntExact(DB_SEDAN_ID),
                        Math.toIntExact(DB_HYBRID_ID))));
    }

    @Test
    @DisplayName("findById: unknown car -> returns 404 Not Found")
    void findById_unknownCar_returnsNotFound() throws Exception {
        mockMvc.perform(get(BASE_URL + "/{id}", UNKNOWN_CAR_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("findAll: default pagination -> returns sorted cars from database")
    void findAll_withoutParameters_returnsSortedCars() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()")
                        .value(INITIAL_CAR_COUNT))
                .andExpect(jsonPath("$.content[0].id")
                        .value(DB_EXISTING_CAR_ID))
                .andExpect(jsonPath("$.content[0].year").value(2020))
                .andExpect(jsonPath("$.content[1].id")
                        .value(DB_SECOND_CAR_ID))
                .andExpect(jsonPath("$.content[2].id")
                        .value(DB_CAR_WITHOUT_CATEGORIES_ID))
                .andExpect(jsonPath("$.content[3].id")
                        .value(DB_BMW_CAR_ID))
                .andExpect(jsonPath("$.page.size").value(12))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements")
                        .value(INITIAL_CAR_COUNT))
                .andExpect(jsonPath("$.page.totalPages").value(1));
    }

    @Test
    @DisplayName("findAll: custom pagination -> returns requested page")
    void findAll_customPagination_returnsRequestedPage() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("page", "1")
                .param("size", "2")
                .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].id")
                        .value(DB_SECOND_CAR_ID))
                .andExpect(jsonPath("$.content[1].id")
                        .value(DB_EXISTING_CAR_ID))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.totalElements")
                        .value(INITIAL_CAR_COUNT))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    @DisplayName("findAll: empty database -> returns empty page")
    void findAll_noCars_returnsEmptyPage() throws Exception {
        jdbcTemplate.update("DELETE FROM car_category");
        jdbcTemplate.update("DELETE FROM car");

        assertThat(carRepository.count()).isZero();

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.page.size").value(12))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(0))
                .andExpect(jsonPath("$.page.totalPages").value(0));
    }

    @Test
    @DisplayName("findAll: combined filters -> returns matching persisted car")
    void findAll_searchParameters_returnsMatchingCar() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("manufacturerId", DB_TOYOTA_ID.toString())
                .param("modelId", DB_COROLLA_ID.toString())
                .param("yearFrom", "2019")
                .param("yearTo", "2020")
                .param("categoryIds",
                        "%d,%d".formatted(DB_SEDAN_ID, DB_HYBRID_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].id")
                        .value(DB_EXISTING_CAR_ID))
                .andExpect(jsonPath("$.content[0].year").value(2020))
                .andExpect(jsonPath("$.content[0].modelId")
                        .value(DB_COROLLA_ID))
                .andExpect(jsonPath("$.content[0].manufacturerId")
                        .value(DB_TOYOTA_ID))
                .andExpect(jsonPath("$.content[0].categoryIds",
                        containsInAnyOrder(
                                Math.toIntExact(DB_SEDAN_ID),
                                Math.toIntExact(DB_HYBRID_ID))))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(1));
    }

    @Test
    @DisplayName("findAll: reversed year range -> returns 400 Bad Request")
    void findAll_reversedYearRange_returnsBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("yearFrom", "2021")
                .param("yearTo", "2020"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("findAll: year before minimum -> returns 400 Bad Request")
    void findAll_yearBeforeMinimum_returnsBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("yearFrom", "1899"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("findAll: future year -> returns 400 Bad Request")
    void findAll_futureYear_returnsBadRequest() throws Exception {
        mockMvc.perform(get(BASE_URL)
                .param("yearTo", Year.now().plusYears(1).toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("update: valid PATCH -> persists year, model and category changes")
    void update_validRequest_persistsChanges() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}", DB_EXISTING_CAR_ID)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.carEditorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "year": 2024,
                            "modelId": %d,
                            "categoryIds": [%d]
                        }
                        """.formatted(DB_CAMRY_ID, DB_SUV_ID)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        var updated = carTestReader.findById(DB_EXISTING_CAR_ID);

        assertThat(updated.year()).isEqualTo(Year.of(2024));
        assertThat(updated.modelId()).isEqualTo(DB_CAMRY_ID);
        assertThat(updated.manufacturerId()).isEqualTo(DB_TOYOTA_ID);
        assertThat(updated.categoryIds()).containsExactly(DB_SUV_ID);

        mockMvc.perform(get(BASE_URL + "/{id}", DB_EXISTING_CAR_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(DB_EXISTING_CAR_ID))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.modelId").value(DB_CAMRY_ID))
                .andExpect(jsonPath("$.manufacturerId").value(DB_TOYOTA_ID))
                .andExpect(jsonPath("$.categoryIds",
                        containsInAnyOrder(Math.toIntExact(DB_SUV_ID))));
    }

    @Test
    @DisplayName("update: partial PATCH -> changes year and preserves other fields")
    void update_yearOnly_preservesOtherFields() throws Exception {
        mockMvc.perform(patch(BASE_URL + "/{id}", DB_EXISTING_CAR_ID)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.carEditorToken())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"year": 2024}
                        """))
                .andExpect(status().isNoContent());

        var updated = carTestReader.findById(DB_EXISTING_CAR_ID);

        assertThat(updated.year()).isEqualTo(Year.of(2024));
        assertThat(updated.modelId()).isEqualTo(DB_COROLLA_ID);
        assertThat(updated.manufacturerId()).isEqualTo(DB_TOYOTA_ID);
        assertThat(updated.categoryIds())
                .containsExactlyInAnyOrder(DB_SEDAN_ID, DB_HYBRID_ID);
    }

    @Test
    @DisplayName("delete: existing car -> removes car and category associations")
    void delete_existingCar_removesCarAndAssociations() throws Exception {
        var initialCount = carRepository.count();

        mockMvc.perform(delete(BASE_URL + "/{id}", DB_EXISTING_CAR_ID)
                .header(HttpHeaders.AUTHORIZATION,
                        "Bearer " + tokens.carEditorToken()))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        assertThat(carRepository.existsById(DB_EXISTING_CAR_ID)).isFalse();

        assertThat(carRepository.count())
                .isEqualTo(initialCount - 1);

        var associationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM car_category WHERE car_id = ?",
                Long.class,
                DB_EXISTING_CAR_ID);

        assertThat(associationCount).isZero();

        assertThat(carRepository.existsById(DB_SECOND_CAR_ID)).isTrue();

        mockMvc.perform(get(BASE_URL + "/{id}", DB_EXISTING_CAR_ID))
                .andExpect(status().isNotFound());
    }
}
