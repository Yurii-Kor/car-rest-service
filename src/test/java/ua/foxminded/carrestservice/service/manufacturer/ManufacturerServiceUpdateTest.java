
package ua.foxminded.carrestservice.service.manufacturer;

import static org.assertj.core.api.Assertions.*;
import static ua.foxminded.carrestservice.service.manufacturer.ManufacturerServiceTestConstants.*;

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
import ua.foxminded.carrestservice.repository.ManufacturerRepository;
import ua.foxminded.carrestservice.service.manufacturer.dto.ManufacturerRequest;
import ua.foxminded.carrestservice.testconfig.PostgreSQLTestConfig;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgreSQLTestConfig.class)
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(
        scripts = "/sql/manufacturer/manufacturer-service-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "/sql/cleanup-database.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class ManufacturerServiceUpdateTest {

    @Autowired
    private ManufacturerService manufacturerService;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Test
    @DisplayName("update: existing manufacturer -> persists new name")
    void update_existingManufacturer_updatesName() {
        var request = new ManufacturerRequest(TOYOTA_NAME_UPDATED);

        manufacturerService.update(TOYOTA_ID, request);

        var updated = manufacturerRepository.findById(TOYOTA_ID).orElseThrow();

        assertThat(updated.getName()).isEqualTo(TOYOTA_NAME_UPDATED);
    }

    @Test
    @DisplayName("update: unchanged name -> preserves existing manufacturer")
    void update_sameName_keepsManufacturerUnchanged() {
        var request = new ManufacturerRequest(TOYOTA_NAME);

        manufacturerService.update(TOYOTA_ID, request);

        var manufacturer = manufacturerRepository.findById(TOYOTA_ID).orElseThrow();

        assertThat(manufacturer.getName()).isEqualTo(TOYOTA_NAME);
    }

    @Test
    @DisplayName("update: duplicate name -> throws EntityExistsException and preserves original name")
    void update_duplicateName_throwsEntityExistsException() {
        var request = new ManufacturerRequest(BMW_NAME);

        assertThatThrownBy(() -> manufacturerService.update(TOYOTA_ID, request))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining(BMW_NAME);

        var manufacturer = manufacturerRepository.findById(TOYOTA_ID).orElseThrow();

        assertThat(manufacturer.getName()).isEqualTo(TOYOTA_NAME);
    }

    @Test
    @DisplayName("update: unknown manufacturer -> throws EntityNotFoundException")
    void update_unknownManufacturer_throwsEntityNotFoundException() {
        var request = new ManufacturerRequest("Honda");

        assertThatThrownBy(() -> manufacturerService.update(UNKNOWN_ID, request))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
