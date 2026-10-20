package ua.foxminded.carrestservice.service.category;

import static org.assertj.core.api.Assertions.*;
import static ua.foxminded.carrestservice.service.category.CategoryServiceTestConstants.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import jakarta.persistence.EntityNotFoundException;
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
class CategoryServiceFindTest {

    @Autowired
    private CategoryService categoryService;

    @Test
    @DisplayName("findById: existing category -> returns entity with correct ID and name")
    void findById_existingCategory_returnsCategory() {
        var category = categoryService.findById(SEDAN_ID);

        assertThat(category.getId()).isEqualTo(SEDAN_ID);
        assertThat(category.getName()).isEqualTo(SEDAN_NAME);
    }

    @Test
    @DisplayName("findById: unknown category -> throws EntityNotFoundException")
    void findById_unknownCategory_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> categoryService.findById(UNKNOWN_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(UNKNOWN_ID.toString());
    }

    @Test
    @DisplayName("findAll: paginated request -> returns sorted categories with correct totals")
    void findAll_returnsSortedPage() {
        var pageable = PageRequest.of(0, 2, Sort.by("name"));

        var page = categoryService.findAll(pageable);

        assertThat(page.getContent())
                .extracting(category -> category.getName())
                .containsExactly(COUPE_NAME, HATCHBACK_NAME);

        assertThat(page.getTotalElements()).isEqualTo(INITIAL_CATEGORIES_COUNT);
        assertThat(page.getTotalPages()).isEqualTo(TOTAL_PAGES);
    }

    @Test
    @DisplayName("findAll: page beyond available results -> returns empty page")
    void findAll_pageBeyondResults_returnsEmptyPage() {
        var pageable = PageRequest.of(3, 2, Sort.by("name"));

        var page = categoryService.findAll(pageable);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(4);
    }

    @Test
    @DisplayName("findAllForSelection: categories exist -> returns all categories sorted by name")
    void findAllForSelection_returnsAllSortedByName() {
        var categories = categoryService.findAllForSelection();

        assertThat(categories)
                .extracting(category -> category.getName())
                .containsExactly(
                        COUPE_NAME,
                        HATCHBACK_NAME,
                        SEDAN_NAME,
                        TRUCK_NAME);
    }
}
