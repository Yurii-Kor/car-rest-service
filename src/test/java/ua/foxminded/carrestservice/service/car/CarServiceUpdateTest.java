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
import ua.foxminded.carrestservice.service.car.dto.CarUpdateRequest;
import ua.foxminded.carrestservice.service.car.exception.CarManufacturerChangeNotAllowedException;
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
class CarServiceUpdateTest {

    @Autowired
    private CarService carService;

    @Test
    @DisplayName("update: year only -> updates year and preserves other fields")
    void update_yearOnly_updatesYear() {
        var request = new CarUpdateRequest(YEAR_2024, null, null);

        carService.update(CAR_ID, request);

        var updated = carService.findById(CAR_ID);

        assertThat(updated.year()).isEqualTo(YEAR_2024);
        assertThat(updated.modelId()).isEqualTo(COROLLA_ID);
        assertThat(updated.manufacturerId()).isEqualTo(TOYOTA_ID);
        assertThat(updated.categoryIds())
                .containsExactlyInAnyOrder(SEDAN_ID, HYBRID_ID);
    }

    @Test
    @DisplayName("update: model from same manufacturer -> changes model")
    void update_modelSameManufacturer_updatesModel() {
        var request = new CarUpdateRequest(null, CAMRY_ID, null);

        carService.update(CAR_ID, request);

        var updated = carService.findById(CAR_ID);

        assertThat(updated.modelId()).isEqualTo(CAMRY_ID);
        assertThat(updated.manufacturerId()).isEqualTo(TOYOTA_ID);
        assertThat(updated.year()).isEqualTo(YEAR_2020);
    }

    @Test
    @DisplayName("update: year and model together -> updates final combination")
    void update_yearAndModel_updatesBothFields() {
        var request = new CarUpdateRequest(
                YEAR_2023, CAMRY_ID, null);

        carService.update(CAR_ID, request);

        var updated = carService.findById(CAR_ID);

        assertThat(updated.year()).isEqualTo(YEAR_2023);
        assertThat(updated.modelId()).isEqualTo(CAMRY_ID);
        assertThat(updated.manufacturerId()).isEqualTo(TOYOTA_ID);
    }

    @Test
    @DisplayName("update: category set -> replaces existing categories")
    void update_categories_replacesCategorySet() {
        var request = new CarUpdateRequest(
                null, null, Set.of(SUV_ID));

        carService.update(CAR_ID, request);

        var updated = carService.findById(CAR_ID);

        assertThat(updated.categoryIds()).containsExactly(SUV_ID);
        assertThat(updated.year()).isEqualTo(YEAR_2020);
        assertThat(updated.modelId()).isEqualTo(COROLLA_ID);
    }

    @Test
    @DisplayName("update: empty category set -> removes all category associations")
    void update_emptyCategories_clearsCategories() {
        var request = new CarUpdateRequest(
                null, null, Set.of());

        carService.update(CAR_ID, request);

        var updated = carService.findById(CAR_ID);

        assertThat(updated.categoryIds()).isEmpty();
    }

    @Test
    @DisplayName("update: all fields omitted -> preserves existing car")
    void update_allFieldsNull_keepsCarUnchanged() {
        var before = carService.findById(CAR_ID);
        var request = new CarUpdateRequest(null, null, null);

        carService.update(CAR_ID, request);

        assertThat(carService.findById(CAR_ID)).isEqualTo(before);
    }

    @Test
    @DisplayName("update: unchanged field values -> does not report duplicate")
    void update_sameValues_keepsCarUnchanged() {
        var before = carService.findById(CAR_ID);

        var request = new CarUpdateRequest(
                YEAR_2020,
                COROLLA_ID,
                Set.of(SEDAN_ID, HYBRID_ID));

        carService.update(CAR_ID, request);

        assertThat(carService.findById(CAR_ID)).isEqualTo(before);
    }

    @Test
    @DisplayName("update: duplicate model and year -> throws EntityExistsException")
    void update_duplicateYear_throwsEntityExistsException() {
        var request = new CarUpdateRequest(YEAR_2022, null, null);

        assertThatThrownBy(() -> carService.update(CAR_ID, request))
                .isInstanceOf(EntityExistsException.class);

        assertThat(carService.findById(CAR_ID).year())
                .isEqualTo(YEAR_2020);
    }

    @Test
    @DisplayName("update: duplicate final model-year combination -> rejects both changes")
    void update_duplicateFinalCombination_throwsEntityExistsException() {
        var request = new CarUpdateRequest(
                YEAR_2021, CAMRY_ID, null);

        assertThatThrownBy(() -> carService.update(CAR_ID, request))
                .isInstanceOf(EntityExistsException.class);

        var unchanged = carService.findById(CAR_ID);

        assertThat(unchanged.year()).isEqualTo(YEAR_2020);
        assertThat(unchanged.modelId()).isEqualTo(COROLLA_ID);
    }

    @Test
    @DisplayName("update: model from another manufacturer -> rejects manufacturer change")
    void update_differentManufacturer_throwsException() {
        var request = new CarUpdateRequest(null, X5_ID, null);

        assertThatThrownBy(() -> carService.update(CAR_ID, request))
                .isInstanceOf(CarManufacturerChangeNotAllowedException.class);

        var unchanged = carService.findById(CAR_ID);

        assertThat(unchanged.modelId()).isEqualTo(COROLLA_ID);
        assertThat(unchanged.manufacturerId()).isEqualTo(TOYOTA_ID);
    }

    @Test
    @DisplayName("update: unknown model -> throws EntityNotFoundException")
    void update_unknownModel_throwsEntityNotFoundException() {
        var request = new CarUpdateRequest(null, UNKNOWN_ID, null);

        assertThatThrownBy(() -> carService.update(CAR_ID, request))
                .isInstanceOf(EntityNotFoundException.class);

        assertThat(carService.findById(CAR_ID).modelId())
                .isEqualTo(COROLLA_ID);
    }

    @Test
    @DisplayName("update: unknown category -> preserves all existing car fields")
    void update_unknownCategory_preservesCar() {
        var before = carService.findById(CAR_ID);

        var request = new CarUpdateRequest(
                YEAR_2024,
                null,
                Set.of(SUV_ID, UNKNOWN_ID));

        assertThatThrownBy(() -> carService.update(CAR_ID, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Category not found");

        assertThat(carService.findById(CAR_ID)).isEqualTo(before);
    }

    @Test
    @DisplayName("update: unknown car -> throws EntityNotFoundException")
    void update_unknownCar_throwsEntityNotFoundException() {
        var request = new CarUpdateRequest(
                YEAR_2024, null, null);

        assertThatThrownBy(() -> carService.update(UNKNOWN_ID, request))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
