
package ua.foxminded.carrestservice.service.car;

import static org.assertj.core.api.Assertions.*;
import static ua.foxminded.carrestservice.service.car.CarServiceTestConstants.*;

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
import ua.foxminded.carrestservice.repository.CarRepository;
import ua.foxminded.carrestservice.repository.CategoryRepository;
import ua.foxminded.carrestservice.testconfig.PostgreSQLTestConfig;
import ua.foxminded.carrestservice.testutil.CarTestReader;

@SpringBootTest
@ActiveProfiles("test")
@Import({
        PostgreSQLTestConfig.class,
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
class CarServiceDeleteTest {

    @Autowired
    private CarService carService;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CarTestReader carTestReader;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("delete: existing car -> removes car and category associations")
    void delete_existingCar_removesCarAndAssociations() {
        carService.delete(CAR_ID);

        assertThat(carRepository.existsById(CAR_ID)).isFalse();

        var associationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM car_category WHERE car_id = ?",
                Long.class,
                CAR_ID);

        assertThat(associationCount).isZero();

        assertThat(carRepository.count())
                .isEqualTo(INITIAL_CAR_COUNT - 1);
    }

    @Test
    @DisplayName("delete: existing car -> preserves categories and other cars")
    void delete_existingCar_preservesRelatedEntities() {
        carService.delete(CAR_ID);

        assertThat(categoryRepository.count()).isEqualTo(3);

        assertThat(categoryRepository.existsById(SEDAN_ID)).isTrue();
        assertThat(categoryRepository.existsById(HYBRID_ID)).isTrue();

        var otherCar = carTestReader.findById(SECOND_CAR_ID);

        assertThat(otherCar.categoryIds()).containsExactly(SEDAN_ID);
    }

    @Test
    @DisplayName("delete: unknown car -> throws EntityNotFoundException")
    void delete_unknownCar_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> carService.delete(UNKNOWN_ID))
                .isInstanceOf(EntityNotFoundException.class);

        assertThat(carRepository.count())
                .isEqualTo(INITIAL_CAR_COUNT);
    }
}
