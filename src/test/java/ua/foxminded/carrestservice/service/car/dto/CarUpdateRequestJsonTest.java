package ua.foxminded.carrestservice.service.car.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Year;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;

import tools.jackson.databind.json.JsonMapper;

@JsonTest
class CarUpdateRequestJsonTest {

    @Autowired
    private JsonMapper jsonMapper;

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("validJsonCases")
    @DisplayName("deserialize: valid partial update JSON -> maps provided fields")
    void deserialize_validJson_mapsProvidedFields(
            String scenario,
            String json,
            CarUpdateRequest expected) {

        var request = jsonMapper.readValue(json, CarUpdateRequest.class);

        assertThat(request).isEqualTo(expected);
    }

    static Stream<Arguments> validJsonCases() {
        return Stream.of(
                Arguments.of(
                        "year only",
                        """
                        {
                            "year": 2021
                        }
                        """,
                        new CarUpdateRequest(
                                Year.of(2021),
                                null,
                                null)),

                Arguments.of(
                        "model ID only",
                        """
                        {
                            "modelId": 10
                        }
                        """,
                        new CarUpdateRequest(
                                null,
                                10L,
                                null)),

                Arguments.of(
                        "categories only",
                        """
                        {
                            "categoryIds": [20, 21]
                        }
                        """,
                        new CarUpdateRequest(
                                null,
                                null,
                                Set.of(20L, 21L))),

                Arguments.of(
                        "empty category array",
                        """
                        {
                            "categoryIds": []
                        }
                        """,
                        new CarUpdateRequest(
                                null,
                                null,
                                Set.of())),

                Arguments.of(
                        "all fields provided",
                        """
                        {
                            "year": 2020,
                            "modelId": 10,
                            "categoryIds": [20, 21]
                        }
                        """,
                        new CarUpdateRequest(
                                Year.of(2020),
                                10L,
                                Set.of(20L, 21L))),

                Arguments.of(
                        "empty JSON object",
                        """
                        {}
                        """,
                        new CarUpdateRequest(
                                null,
                                null,
                                null)),

                Arguments.of(
                        "all fields explicitly null",
                        """
                        {
                            "year": null,
                            "modelId": null,
                            "categoryIds": null
                        }
                        """,
                        new CarUpdateRequest(
                                null,
                                null,
                                null)));
    }

    @Test
    @DisplayName("deserialize: duplicate category IDs -> produces unique category set")
    void deserialize_duplicateCategories_returnsUniqueSet() {
        var request = jsonMapper.readValue(
                """
                {
                    "categoryIds": [20, 20, 21]
                }
                """,
                CarUpdateRequest.class);

        assertThat(request.categoryIds())
                .containsExactlyInAnyOrder(20L, 21L);
    }

    @Test
    @DisplayName("deserialize: null category element -> preserves null for validation")
    void deserialize_nullCategoryElement_preservesNull() {
        var request = jsonMapper.readValue(
                """
                {
                    "categoryIds": [20, null]
                }
                """,
                CarUpdateRequest.class);

        assertThat(request.categoryIds())
                .containsExactlyInAnyOrder(20L, null);
    }

    @Test
    @DisplayName("deserialize: invalid production year -> deserializes value for validation")
    void deserialize_yearBeforeMinimum_preservesYear() {
        var request = jsonMapper.readValue(
                """
                {
                    "year": 1899
                }
                """,
                CarUpdateRequest.class);

        assertThat(request.year()).isEqualTo(Year.of(1899));
    }
}
