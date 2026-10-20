
package ua.foxminded.carrestservice.service.carmodel.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

class CarModelCreateRequestValidatorTest {

    private static final Long MANUFACTURER_ID = 101L;

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("validCases")
    @DisplayName("validate: valid car model create request -> produces no constraint violations")
    void validate_validRequest_noViolations(
            String scenario,
            String name,
            Long manufacturerId) {

        var request = new CarModelCreateRequest(name, manufacturerId);

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    static Stream<Arguments> validCases() {
        return Stream.of(
                Arguments.of(
                        "simple name",
                        "Corolla",
                        MANUFACTURER_ID),

                Arguments.of(
                        "name with spaces",
                        "Corolla Cross",
                        MANUFACTURER_ID),

                Arguments.of(
                        "one character",
                        "A",
                        MANUFACTURER_ID),

                Arguments.of(
                        "maximum length of 255",
                        "A".repeat(255),
                        MANUFACTURER_ID),

                Arguments.of(
                        "minimum positive manufacturer ID",
                        "Corolla",
                        1L));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("invalidCases")
    @DisplayName("validate: invalid car model create request -> reports expected constraint")
    void validate_invalidRequest_reportsExpectedConstraint(
            String scenario,
            String name,
            Long manufacturerId,
            String expectedField,
            Class<? extends Annotation> expectedConstraint) {

        var request = new CarModelCreateRequest(name, manufacturerId);

        var violations = validator.validate(request);

        assertThat(violations).hasSize(1);

        assertThat(violations).allSatisfy(violation -> {
            assertThat(violation.getPropertyPath().toString())
                    .isEqualTo(expectedField);

            assertThat(violation.getConstraintDescriptor()
                    .getAnnotation().annotationType())
                    .isEqualTo(expectedConstraint);
        });
    }

    static Stream<Arguments> invalidCases() {
        return Stream.of(
                Arguments.of(
                        "null name",
                        null,
                        MANUFACTURER_ID,
                        "name",
                        NotBlank.class),

                Arguments.of(
                        "empty name",
                        "",
                        MANUFACTURER_ID,
                        "name",
                        NotBlank.class),

                Arguments.of(
                        "blank name",
                        "   \t   ",
                        MANUFACTURER_ID,
                        "name",
                        NotBlank.class),

                Arguments.of(
                        "length exceeds 255",
                        "A".repeat(256),
                        MANUFACTURER_ID,
                        "name",
                        Size.class),

                Arguments.of(
                        "null manufacturer ID",
                        "Corolla",
                        null,
                        "manufacturerId",
                        NotNull.class),

                Arguments.of(
                        "zero manufacturer ID",
                        "Corolla",
                        0L,
                        "manufacturerId",
                        Positive.class),

                Arguments.of(
                        "negative manufacturer ID",
                        "Corolla",
                        -1L,
                        "manufacturerId",
                        Positive.class));
    }
}
