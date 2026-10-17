package ua.foxminded.carrestservice.model.converter;

import java.time.Year;
import java.util.Optional;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class YearShortConverter implements AttributeConverter<Year, Short> {

    @Override
    public Short convertToDatabaseColumn(Year year) {
        return Optional.ofNullable(year)
                .map(Year::getValue)
                .map(value -> {
                    if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                        throw new IllegalArgumentException(
                                "Year is out of SMALLINT range: %d"
                                        .formatted(value));
                    }

                    return value.shortValue();
                })
                .orElse(null);
    }

    @Override
    public Year convertToEntityAttribute(Short value) {
        return Optional.ofNullable(value)
                .map(Short::intValue)
                .map(Year::of)
                .orElse(null);
    }
}
