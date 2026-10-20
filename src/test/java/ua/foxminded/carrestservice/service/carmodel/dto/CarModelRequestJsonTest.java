package ua.foxminded.carrestservice.service.carmodel.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import tools.jackson.databind.json.JsonMapper;

@JsonTest
class CarModelRequestJsonTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("deserialize: model name with extra spaces -> returns normalized name")
    void deserialize_normalizesSpaces() {
        var request = jsonMapper.readValue(
                """
                {"name": "   Corolla    Cross   "}
                """,
                CarModelUpdateRequest.class);

        assertThat(request.name()).isEqualTo("Corolla Cross");
    }
}
