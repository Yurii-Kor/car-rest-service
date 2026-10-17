package ua.foxminded.carrestservice.service.car.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.annotation.Annotation;
import java.time.Year;
import java.util.Collections;
import java.util.Set;
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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import ua.foxminded.carrestservice.common.validation.MinYear;

class CarUpdateRequestValidatorTest {

    private static final Long MODEL_ID = 10L;
    private static final Long CATEGORY_ID = 20L;

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
    @DisplayName("validate: valid car update request -> produces no constraint violations")
    void validate_validRequest_noViolations(
            String scenario,
            Year year,
            Long modelId,
            Set<Long> categoryIds) {

        var request = new CarUpdateRequest(year, modelId, categoryIds);

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    static Stream<Arguments> validCases() {
        return Stream.of(
                Arguments.of(
                        "all fields null",
                        null,
                        null,
                        null),

                Arguments.of(
                        "year only",
                        Year.of(2020),
                        null,
                        null),

                Arguments.of(
                        "model ID only",
                        null,
                        MODEL_ID,
                        null),

                Arguments.of(
                        "categories only",
                        null,
                        null,
                        Set.of(CATEGORY_ID)),

                Arguments.of(
                        "empty category set",
                        null,
                        null,
                        Set.of()),

                Arguments.of(
                        "minimum year",
                        Year.of(1900),
                        MODEL_ID,
                        Set.of(CATEGORY_ID)),

                Arguments.of(
                        "current year",
                        Year.now(),
                        MODEL_ID,
                        Set.of(CATEGORY_ID)),

                Arguments.of(
                        "multiple categories",
                        Year.of(2020),
                        MODEL_ID,
                        Set.of(20L, 21L)));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("invalidCases")
    @DisplayName("validate: invalid car update request -> reports expected constraint")
    void validate_invalidRequest_reportsExpectedConstraint(
            String scenario,
            Year year,
            Long modelId,
            Set<Long> categoryIds,
            String expectedField,
            Class<? extends Annotation> expectedConstraint) {

        var request = new CarUpdateRequest(year, modelId, categoryIds);

        var violations = validator.validate(request);

        assertThat(violations).hasSize(1);

        var violation = violations.iterator().next();

        assertThat(violation.getPropertyPath().toString())
                .startsWith(expectedField);

        assertThat(violation.getConstraintDescriptor()
                .getAnnotation().annotationType())
                .isEqualTo(expectedConstraint);
    }

    static Stream<Arguments> invalidCases() {
        return Stream.of(
                Arguments.of(
                        "year before 1900",
                        Year.of(1899),
                        null,
                        null,
                        "year",
                        MinYear.class),

                Arguments.of(
                        "year zero",
                        Year.of(0),
                        null,
                        null,
                        "year",
                        MinYear.class),

                Arguments.of(
                        "future year",
                        Year.now().plusYears(1),
                        null,
                        null,
                        "year",
                        PastOrPresent.class),

                Arguments.of(
                        "null category element",
                        null,
                        null,
                        Collections.singleton(null),
                        "categoryIds",
                        NotNull.class));
    }
}
