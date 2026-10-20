
package ua.foxminded.carrestservice.service.carmodel.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import tools.jackson.databind.json.JsonMapper;

@JsonTest
class CarModelCreateRequestJsonTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("deserialize: model name with extra spaces -> normalizes name and maps manufacturer ID")
    void deserialize_normalizesSpacesAndMapsManufacturerId() {
        var request = jsonMapper.readValue(
                """
                {
                    "name": "   Corolla    Cross   ",
                    "manufacturerId": 101
                }
                """,
                CarModelCreateRequest.class);

        assertThat(request.name()).isEqualTo("Corolla Cross");
        assertThat(request.manufacturerId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("deserialize: null fields -> preserves null values for validation")
    void deserialize_nullFields_preservesNull() {
        var request = jsonMapper.readValue(
                """
                {
                    "name": null,
                    "manufacturerId": null
                }
                """,
                CarModelCreateRequest.class);

        assertThat(request.name()).isNull();
        assertThat(request.manufacturerId()).isNull();
    }

    @Test
    @DisplayName("deserialize: missing manufacturer ID -> maps null for validation")
    void deserialize_missingManufacturerId_returnsNull() {
        var request = jsonMapper.readValue(
                """
                {
                    "name": "Corolla"
                }
                """,
                CarModelCreateRequest.class);

        assertThat(request.name()).isEqualTo("Corolla");
        assertThat(request.manufacturerId()).isNull();
    }
}
