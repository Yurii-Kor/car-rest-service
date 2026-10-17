package ua.foxminded.carrestservice.service.category.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import tools.jackson.databind.json.JsonMapper;

@JsonTest
class CategoryRequestJsonTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    @DisplayName("deserialize: category name with extra spaces -> returns normalized name")
    void deserialize_normalizesSpaces() {
        var request = jsonMapper.readValue(
                """
                {"name": "   Sports    Car   "}
                """,
                CategoryRequest.class);

        assertThat(request.name()).isEqualTo("Sports Car");
    }

    @Test
    @DisplayName("deserialize: category name with tabs and spaces -> returns normalized name")
    void deserialize_normalizesTabsAndSpaces() {
        var request = jsonMapper.readValue(
                "{\"name\": \"  Sports\\t  Car  \"}",
                CategoryRequest.class);

        assertThat(request.name()).isEqualTo("Sports Car");
    }

    @Test
    @DisplayName("deserialize: null category name -> preserves null")
    void deserialize_preservesNull() {
        var request = jsonMapper.readValue(
                """
                {"name": null}
                """,
                CategoryRequest.class);

        assertThat(request.name()).isNull();
    }

    @Test
    @DisplayName("deserialize: blank category name -> returns empty string")
    void deserialize_normalizesBlankString() {
        var request = jsonMapper.readValue(
                """
                {"name": "      "}
                """,
                CategoryRequest.class);

        assertThat(request.name()).isEmpty();
    }
}
