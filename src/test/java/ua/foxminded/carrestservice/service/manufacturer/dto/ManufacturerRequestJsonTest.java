package ua.foxminded.carrestservice.service.manufacturer.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import tools.jackson.databind.json.JsonMapper;

@JsonTest
class ManufacturerRequestJsonTest {

    @Autowired
    private JsonMapper jsonMapper;

    @Test
    void deserialize_normalizesSpaces() {
        var request = jsonMapper.readValue(
                """
                {"name": "   Land    Rover   "}
                """,
                ManufacturerRequest.class);

        assertThat(request.name()).isEqualTo("Land Rover");
    }

    @Test
    void deserialize_normalizesTabsAndSpaces() {
        var request = jsonMapper.readValue(
                "{\"name\": \"  BMW\\t  Motors  \"}",
                ManufacturerRequest.class);

        assertThat(request.name()).isEqualTo("BMW Motors");
    }

    @Test
    void deserialize_preservesNull() {
        var request = jsonMapper.readValue(
                """
                {"name": null}
                """,
                ManufacturerRequest.class);

        assertThat(request.name()).isNull();
    }

    @Test
    void deserialize_normalizesBlankString() {
        var request = jsonMapper.readValue(
                """
                {"name": "      "}
                """,
                ManufacturerRequest.class);

        assertThat(request.name()).isEmpty();
    }
}
