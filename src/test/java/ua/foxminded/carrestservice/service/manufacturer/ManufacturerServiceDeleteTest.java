
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

import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carrestservice.repository.ManufacturerRepository;
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
class ManufacturerServiceDeleteTest {

    @Autowired
    private ManufacturerService manufacturerService;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Test
    @DisplayName("delete: manufacturer without models -> removes entity from database")
    void delete_manufacturerWithoutModels_removesManufacturer() {
        manufacturerService.delete(UNUSED_ID);

        assertThat(manufacturerRepository.existsById(UNUSED_ID)).isFalse();
        assertThat(manufacturerRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("delete: unknown manufacturer -> throws EntityNotFoundException without modifying data")
    void delete_unknownManufacturer_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> manufacturerService.delete(UNKNOWN_ID))
                .isInstanceOf(EntityNotFoundException.class);

        assertThat(manufacturerRepository.count()).isEqualTo(4);
    }
}
