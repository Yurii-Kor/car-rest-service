package ua.foxminded.carrestservice.service.car.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Year;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import tools.jackson.databind.json.JsonMapper;

@JsonTest
class CarRequestJsonTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("deserialize: valid car JSON -> converts numeric year and category IDs")
    void deserialize_validRequest_mapsAllFields() {
        var request = jsonMapper.readValue(
                """
                {
                    "year": 2020,
                    "modelId": 10,
                    "categoryIds": [20, 21]
                }
                """,
                CarCreateRequest.class);

        assertThat(request.year()).isEqualTo(Year.of(2020));
        assertThat(request.modelId()).isEqualTo(10L);
        assertThat(request.categoryIds())
                .containsExactlyInAnyOrder(20L, 21L);
    }

    @Test
    @DisplayName("deserialize: empty category array -> returns empty category set")
    void deserialize_emptyCategories_returnsEmptySet() {
        var request = jsonMapper.readValue(
                """
                {
                    "year": 2020,
                    "modelId": 10,
                    "categoryIds": []
                }
                """,
                CarCreateRequest.class);

        assertThat(request.year()).isEqualTo(Year.of(2020));
        assertThat(request.categoryIds()).isEmpty();
    }

    @Test
    @DisplayName("deserialize: null fields -> preserves null values for validation")
    void deserialize_nullFields_preservesNull() {
        var request = jsonMapper.readValue(
                """
                {
                    "year": null,
                    "modelId": null,
                    "categoryIds": null
                }
                """,
                CarCreateRequest.class);

        assertThat(request.year()).isNull();
        assertThat(request.modelId()).isNull();
        assertThat(request.categoryIds()).isNull();
    }
}
