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
class ManufacturerServiceCreateTest {

    @Autowired
    private ManufacturerService manufacturerService;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Test
    @DisplayName("create: new manufacturer -> persists entity and returns generated ID")
    void create_newManufacturer_persistsAndReturnsId() {
        var request = new ManufacturerRequest(HONDA_NAME);

        var id = manufacturerService.create(request);

        assertThat(id).isNotNull();

        var saved = manufacturerRepository.findById(id).orElseThrow();

        assertThat(saved.getName()).isEqualTo(HONDA_NAME);
    }

    @Test
    @DisplayName("create: duplicate name -> throws EntityExistsException without creating entity")
    void create_duplicateName_throwsEntityExistsException() {
        var request = new ManufacturerRequest(TOYOTA_NAME);

        assertThatThrownBy(() -> manufacturerService.create(request))
                .isInstanceOf(EntityExistsException.class)
                .hasMessageContaining(TOYOTA_NAME);

        assertThat(manufacturerRepository.count()).isEqualTo(4);
    }
}
