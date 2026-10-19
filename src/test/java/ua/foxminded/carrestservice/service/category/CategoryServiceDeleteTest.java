
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

import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.repository.CategoryRepository;
import ua.foxminded.carrestservice.service.exception.EntityInUseException;
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
class CategoryServiceDeleteTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("delete: category without cars -> removes category and preserves existing car associations")
    void delete_categoryWithoutCars_removesCategory() {
        categoryService.delete(HATCHBACK_ID);

        assertThat(categoryRepository.existsById(HATCHBACK_ID)).isFalse();
        assertThat(categoryRepository.count()).isEqualTo(3);
        assertThat(categoryRepository.existsById(SEDAN_ID)).isTrue();

        var associationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM car_category WHERE category_id = ?",
                Long.class,
                SEDAN_ID);

        assertThat(associationCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("delete: category with cars -> rejects deletion and preserves related data")
    void delete_categoryWithCars_throwsEntityInUseException() {
        assertThatThrownBy(() -> categoryService.delete(SEDAN_ID))
                .isInstanceOf(EntityInUseException.class)
                .hasMessage(
                        "Cannot delete category with existing cars: id=%d"
                                .formatted(SEDAN_ID));

        assertThat(categoryRepository.existsById(SEDAN_ID)).isTrue();
        assertThat(categoryRepository.count()).isEqualTo(4);

        var associationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM car_category WHERE category_id = ?",
                Long.class,
                SEDAN_ID);

        assertThat(associationCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("delete: unknown category -> throws EntityNotFoundException without modifying data")
    void delete_unknownCategory_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> categoryService.delete(UNKNOWN_ID))
                .isInstanceOf(EntityNotFoundException.class);

        assertThat(categoryRepository.count()).isEqualTo(4);
    }
}
