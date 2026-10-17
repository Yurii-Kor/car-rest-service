package ua.foxminded.carrestservice.service.car;

import static org.assertj.core.api.Assertions.*;
import static ua.foxminded.carrestservice.service.car.CarServiceTestConstants.*;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.repository.CarRepository;
import ua.foxminded.carrestservice.service.car.dto.CarRequest;
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
class CarServiceCreateTest {

    @Autowired
    private CarService carService;

    @Autowired
    private CarRepository carRepository;

    @Test
    @DisplayName("create: valid request -> persists car and returns generated ID")
    void create_validRequest_persistsCarAndReturnsId() {
        var request = new CarRequest(
                YEAR_2023,
                COROLLA_ID,
                Set.of(SEDAN_ID, SUV_ID));

        var id = carService.create(request);

        assertThat(id).isNotNull();

        var saved = carService.findById(id);

        assertThat(saved.year()).isEqualTo(YEAR_2023);
        assertThat(saved.modelId()).isEqualTo(COROLLA_ID);
        assertThat(saved.manufacturerId()).isEqualTo(TOYOTA_ID);
        assertThat(saved.categoryIds())
                .containsExactlyInAnyOrder(SEDAN_ID, SUV_ID);

        assertThat(carRepository.count())
                .isEqualTo(INITIAL_CAR_COUNT + 1);
    }

    @Test
    @DisplayName("create: empty category set -> persists car without categories")
    void create_emptyCategories_persistsCarWithoutCategories() {
        var request = new CarRequest(
                YEAR_2023,
                COROLLA_ID,
                Set.of());

        var id = carService.create(request);

        var saved = carService.findById(id);

        assertThat(saved.categoryIds()).isEmpty();
        assertThat(saved.year()).isEqualTo(YEAR_2023);
        assertThat(saved.modelId()).isEqualTo(COROLLA_ID);
    }

    @Test
    @DisplayName("create: duplicate model and year -> throws EntityExistsException")
    void create_duplicateCombination_throwsEntityExistsException() {
        var request = new CarRequest(
                YEAR_2020,
                COROLLA_ID,
                Set.of(SEDAN_ID));

        assertThatThrownBy(() -> carService.create(request))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining("modelId=11, year=2020");

        assertThat(carRepository.count())
                .isEqualTo(INITIAL_CAR_COUNT);
    }

    @Test
    @DisplayName("create: unknown model -> throws EntityNotFoundException")
    void create_unknownModel_throwsEntityNotFoundException() {
        var request = new CarRequest(
                YEAR_2023,
                UNKNOWN_ID,
                Set.of(SEDAN_ID));

        assertThatThrownBy(() -> carService.create(request))
                .isInstanceOf(EntityNotFoundException.class);

        assertThat(carRepository.count())
                .isEqualTo(INITIAL_CAR_COUNT);
    }

    @Test
    @DisplayName("create: unknown category -> throws EntityNotFoundException without persisting car")
    void create_unknownCategory_throwsEntityNotFoundException() {
        var request = new CarRequest(
                YEAR_2023,
                COROLLA_ID,
                Set.of(SEDAN_ID, UNKNOWN_ID));

        assertThatThrownBy(() -> carService.create(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found");

        assertThat(carRepository.count())
                .isEqualTo(INITIAL_CAR_COUNT);
    }
}
