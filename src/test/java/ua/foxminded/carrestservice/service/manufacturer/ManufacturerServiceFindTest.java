
package ua.foxminded.carrestservice.service.manufacturer;

import static org.assertj.core.api.Assertions.*;
import static ua.foxminded.carrestservice.service.manufacturer.ManufacturerServiceTestConstants.*;

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
        scripts = "/sql/manufacturer/manufacturer-service-base.sql",
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
@Sql(
        scripts = "/sql/cleanup-database.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
class ManufacturerServiceFindTest {

    @Autowired
    private ManufacturerService manufacturerService;

    @Test
    @DisplayName("findById: existing manufacturer -> returns entity with correct ID and name")
    void findById_existingManufacturer_returnsManufacturer() {
        var manufacturer = manufacturerService.findById(TOYOTA_ID);

        assertThat(manufacturer.getId()).isEqualTo(TOYOTA_ID);
        assertThat(manufacturer.getName()).isEqualTo(TOYOTA_NAME);
    }

    @Test
    @DisplayName("findById: unknown manufacturer -> throws EntityNotFoundException")
    void findById_unknownManufacturer_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> manufacturerService.findById(UNKNOWN_ID))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(UNKNOWN_ID.toString());
    }

    @Test
    @DisplayName("findAll: paginated request -> returns sorted page with correct totals")
    void findAll_returnsSortedPage() {
        var pageable = PageRequest.of(0, 2, Sort.by("name"));

        var page = manufacturerService.findAll(pageable);

        assertThat(page.getContent())
                .extracting(manufacturer -> manufacturer.getName())
                .containsExactly(BMW_NAME, PROTOTYPE_NAME);

        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("findAllForSelection: manufacturers exist -> returns all entities sorted by name")
    void findAllForSelection_returnsAllSortedByName() {
        var manufacturers = manufacturerService.findAllForSelection();

        assertThat(manufacturers)
                .extracting(manufacturer -> manufacturer.getName())
                .containsExactly(
                        BMW_NAME,
                        PROTOTYPE_NAME,
                        TOYOTA_NAME,
                        UNUSED_NAME);
    }
}
