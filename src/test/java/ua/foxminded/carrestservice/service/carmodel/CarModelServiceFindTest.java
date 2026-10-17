
package ua.foxminded.carrestservice.service.carmodel;

import static org.assertj.core.api.Assertions.*;
import static ua.foxminded.carrestservice.service.carmodel.CarModelServiceTestConstants.*;

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
        scripts = "/sql/carmodel/car-model-service-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "/sql/cleanup-database.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class CarModelServiceFindTest {

    @Autowired
    private CarModelService carModelService;

    @Test
    @DisplayName("findById: existing model -> returns entity with correct ID and name")
    void findById_existingModel_returnsModel() {
        var carModel = carModelService.findById(COROLLA_ID);

        assertThat(carModel.getId()).isEqualTo(COROLLA_ID);
        assertThat(carModel.getName()).isEqualTo(COROLLA_NAME);
    }

    @Test
    @DisplayName("findById: unknown model -> throws EntityNotFoundException")
    void findById_unknownModel_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> carModelService.findById(UNKNOWN_MODEL_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(UNKNOWN_MODEL_ID.toString());
    }

    @Test
    @DisplayName("findAllByManufacturerId: paginated request -> returns sorted manufacturer models with correct totals")
    void findAllByManufacturerId_returnsSortedPage() {
        var pageable = PageRequest.of(0, 1, Sort.by("name"));

        var page = carModelService.findAllByManufacturerId(
                TOYOTA_ID, pageable);

        assertThat(page.getContent())
                .extracting(carModel -> carModel.getName())
                .containsExactly(CAMRY_NAME);

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("findAllByManufacturerId: manufacturer without models -> returns empty page")
    void findAllByManufacturerId_noModels_returnsEmptyPage() {
        var pageable = PageRequest.of(0, 10, Sort.by("name"));

        var page = carModelService.findAllByManufacturerId(
                EMPTY_MANUFACTURER_ID, pageable);

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("findAllByManufacturerId: unknown manufacturer -> throws EntityNotFoundException")
    void findAllByManufacturerId_unknownManufacturer_throwsEntityNotFoundException() {
        var pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> carModelService.findAllByManufacturerId(
                UNKNOWN_MANUFACTURER_ID, pageable))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("findAllForSelection: manufacturer has models -> returns all models sorted by name")
    void findAllForSelection_returnsAllSortedByName() {
        var models = carModelService.findAllForSelection(TOYOTA_ID);

        assertThat(models)
                .extracting(carModel -> carModel.getName())
                .containsExactly(CAMRY_NAME, COROLLA_NAME);
    }

    @Test
    @DisplayName("findAllForSelection: unknown manufacturer -> throws EntityNotFoundException")
    void findAllForSelection_unknownManufacturer_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> carModelService.findAllForSelection(
                UNKNOWN_MANUFACTURER_ID))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
