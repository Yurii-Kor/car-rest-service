package ua.foxminded.carrestservice.service.category;

import static org.assertj.core.api.Assertions.*;
import static ua.foxminded.carrestservice.service.category.CategoryServiceTestConstants.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.repository.CategoryRepository;
import ua.foxminded.carrestservice.service.category.dto.CategoryRequest;
import ua.foxminded.carrestservice.testconfig.PostgreSQLTestConfig;

@SpringBootTest
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
class CategoryServiceUpdateTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("update: existing category -> persists new name")
    void update_existingCategory_updatesName() {
        var request = new CategoryRequest("Crossover");

        categoryService.update(HATCHBACK_ID, request);

        var updated = categoryRepository.findById(HATCHBACK_ID)
                .orElseThrow();

        assertThat(updated.getName()).isEqualTo("Crossover");
        assertThat(categoryRepository.existsByName("Crossover")).isTrue();
    }

    @Test
    @DisplayName("update: unchanged name -> preserves existing category")
    void update_sameName_keepsCategoryUnchanged() {
        var request = new CategoryRequest(HATCHBACK_NAME);

        categoryService.update(HATCHBACK_ID, request);

        var category = categoryRepository.findById(HATCHBACK_ID)
                .orElseThrow();

        assertThat(category.getName()).isEqualTo(HATCHBACK_NAME);
        assertThat(categoryRepository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("update: duplicate category name -> throws EntityExistsException and preserves original name")
    void update_duplicateName_throwsEntityExistsException() {
        var request = new CategoryRequest(SEDAN_NAME);

        assertThatThrownBy(() -> categoryService.update(
                HATCHBACK_ID, request))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining(SEDAN_NAME);

        var category = categoryRepository.findById(HATCHBACK_ID)
                .orElseThrow();

        assertThat(category.getName()).isEqualTo(HATCHBACK_NAME);
    }

    @Test
    @DisplayName("update: unknown category -> throws EntityNotFoundException")
    void update_unknownCategory_throwsEntityNotFoundException() {
        var request = new CategoryRequest("Crossover");

        assertThatThrownBy(() -> categoryService.update(
                UNKNOWN_ID, request))
                .isInstanceOf(EntityNotFoundException.class);

        assertThat(categoryRepository.count()).isEqualTo(4);
    }

    @Test
    @DisplayName("update: category assigned to car -> updates name and preserves car association")
    void update_categoryWithCar_preservesAssociation() {
        var request = new CategoryRequest("Family Sedan");

        categoryService.update(SEDAN_ID, request);

        var updated = categoryRepository.findById(SEDAN_ID)
                .orElseThrow();

        assertThat(updated.getName()).isEqualTo("Family Sedan");

        var associationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM car_category WHERE category_id = ?",
                Long.class,
                SEDAN_ID);

        assertThat(associationCount).isEqualTo(1L);
    }
}
