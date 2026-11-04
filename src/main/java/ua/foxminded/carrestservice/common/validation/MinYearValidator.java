package ua.foxminded.carrestservice.common.validation;

import java.time.Year;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MinYearValidator implements ConstraintValidator<MinYear, Year> {

    private Year minYear;

    @Override
    public void initialize(MinYear annotation) {
        minYear = Year.of(annotation.value());
    }

    @Override
    public boolean isValid(Year year, ConstraintValidatorContext context) {
        return year == null || !year.isBefore(minYear);
    }
}
