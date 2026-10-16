
package ua.foxminded.carrestservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import jakarta.persistence.EntityManager;
import ua.foxminded.carrestservice.model.Manufacturer;
import ua.foxminded.carrestservice.testconfig.PostgreSQLTestConfig;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(PostgreSQLTestConfig.class)
class ManufacturerRepositoryTest {

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void save_persistsManufacturer() {
        Manufacturer manufacturer = new Manufacturer("Toyota");

        Manufacturer saved = manufacturerRepository.saveAndFlush(manufacturer);
        Long id = saved.getId();

        entityManager.clear();

        assertThat(id).isNotNull();

        Manufacturer found = manufacturerRepository.findById(id).orElseThrow();

        assertThat(found.getName()).isEqualTo("Toyota");
    }
}
