package ua.foxminded.carrestservice.service.category.dto;

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
import jakarta.validation.constraints.Size;

class CategoryRequestValidatorTest {

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
    @DisplayName("validate: valid category name -> produces no constraint violations")
    void validate_validName_noViolations(String scenario, String name) {
        var request = new CategoryRequest(name);

        var violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    static Stream<Arguments> validCases() {
        return Stream.of(
                Arguments.of("simple name", "Sedan"),
                Arguments.of("name with spaces", "Sports Car"),
                Arguments.of("one character", "A"),
                Arguments.of("maximum length of 255", "A".repeat(255)));
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("invalidCases")
    @DisplayName("validate: invalid category name -> reports expected constraint on name")
    void validate_invalidName_reportsExpectedConstraint(
            String scenario,
            String name,
            Class<? extends Annotation> expectedConstraint) {

        var request = new CategoryRequest(name);

        var violations = validator.validate(request);

        assertThat(violations).hasSize(1);

        assertThat(violations).allSatisfy(violation -> {
            assertThat(violation.getPropertyPath().toString())
                    .isEqualTo("name");

            assertThat(violation.getConstraintDescriptor()
                    .getAnnotation().annotationType())
                    .isEqualTo(expectedConstraint);
        });
    }

    static Stream<Arguments> invalidCases() {
        return Stream.of(
                Arguments.of("null name", null, NotBlank.class),
                Arguments.of("empty name", "", NotBlank.class),
                Arguments.of("blank name", "   \t   ", NotBlank.class),
                Arguments.of("length exceeds 255", "A".repeat(256), Size.class));
    }
}
