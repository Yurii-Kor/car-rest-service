package ua.foxminded.carrestservice.model.converter;

import static org.assertj.core.api.Assertions.*;

import java.time.Year;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class YearShortConverterTest {

    private final YearShortConverter converter = new YearShortConverter();

    @ParameterizedTest(name = "[{index}] year={0}")
    @ValueSource(ints = {
            Short.MIN_VALUE,
            1900,
            2020,
            Short.MAX_VALUE
    })
    @DisplayName("convertToDatabaseColumn: year within SMALLINT range -> returns corresponding Short")
    void convertToDatabaseColumn_validYear_returnsShort(int value) {
        var year = Year.of(value);

        var result = converter.convertToDatabaseColumn(year);

        assertThat(result).isEqualTo((short) value);
    }

    @Test
    @DisplayName("convertToDatabaseColumn: null year -> returns null")
    void convertToDatabaseColumn_null_returnsNull() {
        var result = converter.convertToDatabaseColumn(null);

        assertThat(result).isNull();
    }

    @ParameterizedTest(name = "[{index}] value={0}")
    @ValueSource(shorts = {
            Short.MIN_VALUE,
            1900,
            2020,
            Short.MAX_VALUE
    })
    @DisplayName("convertToEntityAttribute: valid Short value -> returns corresponding Year")
    void convertToEntityAttribute_validValue_returnsYear(short value) {
        var result = converter.convertToEntityAttribute(value);

        assertThat(result).isEqualTo(Year.of(value));
    }

    @Test
    @DisplayName("convertToEntityAttribute: null value -> returns null")
    void convertToEntityAttribute_null_returnsNull() {
        var result = converter.convertToEntityAttribute(null);

        assertThat(result).isNull();
    }
}
