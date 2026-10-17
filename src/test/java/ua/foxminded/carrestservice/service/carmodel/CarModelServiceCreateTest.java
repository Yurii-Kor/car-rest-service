
package ua.foxminded.carrestservice.service.carmodel;

import static org.assertj.core.api.Assertions.*;
import static ua.foxminded.carrestservice.service.carmodel.CarModelServiceTestConstants.*;

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
import ua.foxminded.carrestservice.repository.CarModelRepository;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelRequest;
import ua.foxminded.carrestservice.testconfig.PostgreSQLTestConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgreSQLTestConfig.class)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(
        scripts = "/sql/carmodel/car-model-service-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "/sql/cleanup-database.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class CarModelServiceCreateTest {

    @Autowired
    private CarModelService carModelService;

    @Autowired
    private CarModelRepository carModelRepository;

    @Test
    @DisplayName("create: new model -> persists entity and returns generated ID")
    void create_newModel_persistsAndReturnsId() {
        var request = new CarModelRequest("Yaris");

        var id = carModelService.create(TOYOTA_ID, request);

        assertThat(id).isNotNull();

        var saved = carModelRepository.findById(id).orElseThrow();

        assertThat(saved.getName()).isEqualTo("Yaris");
        assertThat(carModelRepository.existsByManufacturerIdAndName(
                TOYOTA_ID, "Yaris")).isTrue();
    }

    @Test
    @DisplayName("create: model name exists under another manufacturer -> creates model successfully")
    void create_nameExistsUnderAnotherManufacturer_createsModel() {
        var request = new CarModelRequest(CAMRY_NAME);

        var id = carModelService.create(HONDA_ID, request);

        assertThat(id).isNotNull();
        assertThat(carModelRepository.existsByManufacturerIdAndName(
                HONDA_ID, CAMRY_NAME)).isTrue();
        assertThat(carModelRepository.count()).isEqualTo(6);
    }

    @Test
    @DisplayName("create: duplicate name within manufacturer -> throws EntityExistsException")
    void create_duplicateName_throwsEntityExistsException() {
        var request = new CarModelRequest(COROLLA_NAME);

        assertThatThrownBy(() -> carModelService.create(TOYOTA_ID, request))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining(COROLLA_NAME);

        assertThat(carModelRepository.count()).isEqualTo(5);
    }

    @Test
    @DisplayName("create: unknown manufacturer -> throws EntityNotFoundException")
    void create_unknownManufacturer_throwsEntityNotFoundException() {
        var request = new CarModelRequest("Yaris");

        assertThatThrownBy(() -> carModelService.create(
                UNKNOWN_MANUFACTURER_ID, request))
                .isInstanceOf(EntityNotFoundException.class);

        assertThat(carModelRepository.count()).isEqualTo(5);
    }
}
