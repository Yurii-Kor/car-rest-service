package ua.foxminded.carrestservice.service.car;

import static org.assertj.core.api.Assertions.*;
import static ua.foxminded.carrestservice.service.car.CarServiceTestConstants.*;

import java.util.Set;

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
import ua.foxminded.carrestservice.service.car.dto.CarDetails;
import ua.foxminded.carrestservice.testconfig.PostgreSQLTestConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgreSQLTestConfig.class)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(
        scripts = "/sql/car/car-service-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "/sql/cleanup-database.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class CarServiceFindTest {

    @Autowired
    private CarService carService;

    @Test
    @DisplayName("findById: existing car -> returns complete CarDetails")
    void findById_existingCar_returnsDetails() {
        var result = carService.findById(CAR_ID);

        assertThat(result).isEqualTo(new CarDetails(
                CAR_ID,
                YEAR_2020,
                COROLLA_ID,
                TOYOTA_ID,
                Set.of(SEDAN_ID, HYBRID_ID)));
    }

    @Test
    @DisplayName("findById: car without categories -> returns empty category set")
    void findById_carWithoutCategories_returnsEmptySet() {
        var result = carService.findById(CAR_WITHOUT_CATEGORIES_ID);

        assertThat(result.id()).isEqualTo(CAR_WITHOUT_CATEGORIES_ID);
        assertThat(result.categoryIds()).isEmpty();
    }

    @Test
    @DisplayName("findById: unknown car -> throws EntityNotFoundException")
    void findById_unknownCar_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> carService.findById(UNKNOWN_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Car not found: id=%d".formatted(UNKNOWN_ID));
    }

    @Test
    @DisplayName("findAll: first page -> returns correct cars and category associations")
    void findAll_firstPage_returnsCorrectCars() {
        var pageable = PageRequest.of(0, 2, Sort.by("id"));

        var page = carService.findAll(pageable);

        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getContent()).hasSize(2);

        assertThat(page.getContent())
                .extracting(CarDetails::id)
                .containsExactly(CAR_ID, SECOND_CAR_ID);

        assertThat(page.getContent().get(0).categoryIds())
                .containsExactlyInAnyOrder(SEDAN_ID, HYBRID_ID);

        assertThat(page.getContent().get(1).categoryIds())
                .containsExactly(SEDAN_ID);
    }

    @Test
    @DisplayName("findAll: second page -> returns remaining cars with correct categories")
    void findAll_secondPage_returnsRemainingCars() {
        var pageable = PageRequest.of(1, 2, Sort.by("id"));

        var page = carService.findAll(pageable);

        assertThat(page.getContent())
                .extracting(CarDetails::id)
                .containsExactly(
                        CAR_WITHOUT_CATEGORIES_ID,
                        BMW_CAR_ID);

        assertThat(page.getContent().get(0).categoryIds())
                .isEmpty();

        assertThat(page.getContent().get(1).categoryIds())
                .containsExactly(SUV_ID);

        assertThat(page.getContent().get(1).manufacturerId())
                .isEqualTo(BMW_ID);
    }

    @Test
    @DisplayName("findAll: page beyond available cars -> returns empty page")
    void findAll_pageBeyondRange_returnsEmptyPage() {
        var pageable = PageRequest.of(3, 2, Sort.by("id"));

        var page = carService.findAll(pageable);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(4);
    }
}
