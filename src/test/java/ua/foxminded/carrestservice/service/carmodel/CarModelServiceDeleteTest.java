package ua.foxminded.carrestservice.service.carmodel;

import static org.assertj.core.api.Assertions.*;
import static ua.foxminded.carrestservice.service.carmodel.CarModelServiceTestConstants.*;

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
import ua.foxminded.carrestservice.repository.CarModelRepository;
import ua.foxminded.carrestservice.repository.ManufacturerRepository;
import ua.foxminded.carrestservice.service.exception.EntityInUseException;
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
class CarModelServiceDeleteTest {

    @Autowired
    private CarModelService carModelService;

    @Autowired
    private CarModelRepository carModelRepository;

    @Autowired
    private ManufacturerRepository manufacturerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("delete: model without cars -> removes model and preserves manufacturer")
    void delete_modelWithoutCars_removesModel() {
        carModelService.delete(CAMRY_ID);

        assertThat(carModelRepository.existsById(CAMRY_ID)).isFalse();
        assertThat(carModelRepository.count()).isEqualTo(INITIAL_CAR_MODELS_COUNT - 1L);
        assertThat(manufacturerRepository.existsById(TOYOTA_ID)).isTrue();
    }

    @Test
    @DisplayName("delete: unknown model -> throws EntityNotFoundException without modifying data")
    void delete_unknownModel_throwsEntityNotFoundException() {
        assertThatThrownBy(() -> carModelService.delete(UNKNOWN_MODEL_ID))
                .isInstanceOf(EntityNotFoundException.class);

        assertThat(carModelRepository.count()).isEqualTo(INITIAL_CAR_MODELS_COUNT);
    }

    @Test
    @DisplayName("delete: model has cars -> rejects deletion and preserves related data")
    void delete_modelWithCars_throwsEntityInUseException() {
        assertThatThrownBy(() -> carModelService.delete(COROLLA_ID))
                .isInstanceOf(EntityInUseException.class)
                .hasMessage(
                        "Cannot delete car model with existing cars: id=%d"
                                .formatted(COROLLA_ID));
    
        assertThat(carModelRepository.existsById(COROLLA_ID)).isTrue();
        assertThat(carModelRepository.count()).isEqualTo(INITIAL_CAR_MODELS_COUNT);
    
        var carCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM car WHERE model_id = ?",
                Long.class,
                COROLLA_ID);
    
        assertThat(carCount).isEqualTo(1L);
    }
}
