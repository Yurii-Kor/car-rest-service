
package ua.foxminded.carrestservice.repository;

import static org.assertj.core.api.Assertions.*;
import static ua.foxminded.carrestservice.repository.CarRepositoryTestConstants.*;

import java.time.Year;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import ua.foxminded.carrestservice.repository.projection.CarCategoryRow;
import ua.foxminded.carrestservice.repository.projection.CarRow;
import ua.foxminded.carrestservice.testconfig.PostgreSQLTestConfig;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.NONE
)
@Import(PostgreSQLTestConfig.class)
@Sql(
        scripts = "/sql/car/car-service-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "/sql/cleanup-database.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class CarRepositoryTest {

    @Autowired
    private CarRepository carRepository;

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("combinationCases")
    @DisplayName("existsByModelIdAndYear: model and year combination -> returns expected result")
    void existsByModelIdAndYear_combination_returnsExpectedResult(
            String scenario,
            Long modelId,
            Year year,
            boolean expected) {

        var result = carRepository.existsByModelIdAndYear(
                modelId, year);

        assertThat(result).isEqualTo(expected);
    }

    static Stream<Arguments> combinationCases() {
        return Stream.of(
                Arguments.of(
                        "existing Corolla 2020",
                        COROLLA_ID, YEAR_2020, true),

                Arguments.of(
                        "existing Corolla 2022",
                        COROLLA_ID, YEAR_2022, true),

                Arguments.of(
                        "existing BMW X5 2020",
                        X5_ID, YEAR_2020, true),

                Arguments.of(
                        "same year but different model",
                        CAMRY_ID, YEAR_2020, false),

                Arguments.of(
                        "existing model but unused year",
                        COROLLA_ID, YEAR_2023, false));
    }

    @Test
    @DisplayName("findRowById: existing car -> returns correct CarRow projection")
    void findRowById_existingCar_returnsProjection() {
        var result = carRepository.findRowById(COROLLA_2020_ID);

        assertThat(result)
                .contains(new CarRow(
                        COROLLA_2020_ID,
                        YEAR_2020,
                        COROLLA_ID,
                        TOYOTA_ID));
    }

    @Test
    @DisplayName("findRowById: unknown car -> returns empty Optional")
    void findRowById_unknownCar_returnsEmptyOptional() {
        var result = carRepository.findRowById(UNKNOWN_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAllRows: first page -> returns correct projections and total count")
    void findAllRows_firstPage_returnsProjections() {
        var pageable = PageRequest.of(
                0, 2, Sort.by("id"));

        var page = carRepository.findAllRows(pageable);

        assertThat(page.getTotalElements()).isEqualTo(TOTAL_CARS);
        assertThat(page.getTotalPages()).isEqualTo(2);

        assertThat(page.getContent()).containsExactly(
                new CarRow(
                        COROLLA_2020_ID,
                        YEAR_2020,
                        COROLLA_ID,
                        TOYOTA_ID),

                new CarRow(
                        CAMRY_2021_ID,
                        YEAR_2021,
                        CAMRY_ID,
                        TOYOTA_ID));
    }

    @Test
    @DisplayName("findAllRows: second page -> returns remaining cars without duplicates")
    void findAllRows_secondPage_returnsRemainingCars() {
        var pageable = PageRequest.of(
                1, 2, Sort.by("id"));

        var page = carRepository.findAllRows(pageable);

        assertThat(page.getContent()).containsExactly(
                new CarRow(
                        COROLLA_2022_ID,
                        YEAR_2022,
                        COROLLA_ID,
                        TOYOTA_ID),

                new CarRow(
                        X5_2020_ID,
                        YEAR_2020,
                        X5_ID,
                        BMW_ID));

        assertThat(page.getTotalElements()).isEqualTo(TOTAL_CARS);
    }

    @Test
    @DisplayName("findAllRows: page beyond available results -> returns empty content with correct total")
    void findAllRows_pageBeyondRange_returnsEmptyPage() {
        var pageable = PageRequest.of(
                3, 2, Sort.by("id"));

        var page = carRepository.findAllRows(pageable);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(TOTAL_CARS);
    }

    @Test
    @DisplayName("findCategoryRowsByCarIds: multiple cars -> returns correct car-category pairs")
    void findCategoryRowsByCarIds_multipleCars_returnsCorrectPairs() {
        var ids = List.of(
                COROLLA_2020_ID,
                CAMRY_2021_ID,
                COROLLA_2022_ID,
                X5_2020_ID);

        var rows = carRepository.findCategoryRowsByCarIds(ids);

        assertThat(rows).containsExactlyInAnyOrder(
                new CarCategoryRow(COROLLA_2020_ID, SEDAN_ID),
                new CarCategoryRow(COROLLA_2020_ID, HYBRID_ID),
                new CarCategoryRow(CAMRY_2021_ID, SEDAN_ID),
                new CarCategoryRow(X5_2020_ID, SUV_ID));
    }

    @Test
    @DisplayName("findCategoryRowsByCarIds: subset of cars -> returns only requested associations")
    void findCategoryRowsByCarIds_subset_returnsOnlyRequestedPairs() {
        var ids = List.of(
                COROLLA_2020_ID,
                COROLLA_2022_ID);

        var rows = carRepository.findCategoryRowsByCarIds(ids);

        assertThat(rows).containsExactlyInAnyOrder(
                new CarCategoryRow(COROLLA_2020_ID, SEDAN_ID),
                new CarCategoryRow(COROLLA_2020_ID, HYBRID_ID));
    }

    @Test
    @DisplayName("findCategoryRowsByCarIds: car without categories -> returns empty list")
    void findCategoryRowsByCarIds_noCategories_returnsEmptyList() {
        var rows = carRepository.findCategoryRowsByCarIds(
                List.of(COROLLA_2022_ID));

        assertThat(rows).isEmpty();
    }

    @Test
    @DisplayName("findCategoryRowsByCarIds: unknown car -> returns empty list")
    void findCategoryRowsByCarIds_unknownCar_returnsEmptyList() {
        var rows = carRepository.findCategoryRowsByCarIds(
                List.of(UNKNOWN_ID));

        assertThat(rows).isEmpty();
    }
}
