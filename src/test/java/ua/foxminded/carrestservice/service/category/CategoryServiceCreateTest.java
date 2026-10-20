package ua.foxminded.carrestservice.service.category;

import static org.assertj.core.api.Assertions.*;
import static ua.foxminded.carrestservice.service.category.CategoryServiceTestConstants.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import jakarta.persistence.EntityExistsException;
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
class CategoryServiceCreateTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("create: new category -> persists entity and returns generated ID")
    void create_newCategory_persistsAndReturnsId() {
        var request = new CategoryRequest(CROSSOVER_NAME);

        var id = categoryService.create(request);

        assertThat(id).isNotNull();

        var saved = categoryRepository.findById(id).orElseThrow();

        assertThat(saved.getName()).isEqualTo(CROSSOVER_NAME);
        assertThat(categoryRepository.existsByName(CROSSOVER_NAME)).isTrue();
        assertThat(categoryRepository.count()).isEqualTo(INITIAL_CATEGORIES_COUNT.intValue() + 1);
    }

    @Test
    @DisplayName("create: duplicate category name -> throws EntityExistsException without modifying data")
    void create_duplicateName_throwsEntityExistsException() {
        var request = new CategoryRequest(SEDAN_NAME);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining(SEDAN_NAME);

        assertThat(categoryRepository.count()).isEqualTo(4);
    }
}
