
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
class CarModelServiceUpdateTest {

    @Autowired
    private CarModelService carModelService;

    @Autowired
    private CarModelRepository carModelRepository;

    @Test
    @DisplayName("update: existing model -> persists new name under same manufacturer")
    void update_existingModel_updatesName() {
        var request = new CarModelRequest("Camry Hybrid");

        carModelService.update(CAMRY_ID, request);

        var updated = carModelRepository.findById(CAMRY_ID).orElseThrow();

        assertThat(updated.getName()).isEqualTo("Camry Hybrid");
        assertThat(carModelRepository.existsByManufacturerIdAndName(
                TOYOTA_ID, "Camry Hybrid")).isTrue();
    }

    @Test
    @DisplayName("update: unchanged name -> preserves existing model")
    void update_sameName_keepsModelUnchanged() {
        var request = new CarModelRequest(CAMRY_NAME);

        carModelService.update(CAMRY_ID, request);

        var carModel = carModelRepository.findById(CAMRY_ID).orElseThrow();

        assertThat(carModel.getName()).isEqualTo(CAMRY_NAME);
    }

    @Test
    @DisplayName("update: duplicate name within manufacturer -> throws EntityExistsException and preserves original name")
    void update_duplicateName_throwsEntityExistsException() {
        var request = new CarModelRequest(COROLLA_NAME);

        assertThatThrownBy(() -> carModelService.update(CAMRY_ID, request))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining(COROLLA_NAME);

        var carModel = carModelRepository.findById(CAMRY_ID).orElseThrow();

        assertThat(carModel.getName()).isEqualTo(CAMRY_NAME);
    }

    @Test
    @DisplayName("update: name exists under another manufacturer -> updates model successfully")
    void update_nameExistsUnderAnotherManufacturer_updatesModel() {
        var request = new CarModelRequest(CIVIC_NAME);

        carModelService.update(CAMRY_ID, request);

        assertThat(carModelRepository.existsByManufacturerIdAndName(
                TOYOTA_ID, CIVIC_NAME)).isTrue();

        assertThat(carModelRepository.existsByManufacturerIdAndName(
                HONDA_ID, CIVIC_NAME)).isTrue();
    }

    @Test
    @DisplayName("update: unknown model -> throws EntityNotFoundException")
    void update_unknownModel_throwsEntityNotFoundException() {
        var request = new CarModelRequest("Yaris");

        assertThatThrownBy(() -> carModelService.update(
                UNKNOWN_MODEL_ID, request))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
