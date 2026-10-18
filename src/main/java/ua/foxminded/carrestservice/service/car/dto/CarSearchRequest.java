package ua.foxminded.carrestservice.service.car.dto;

import java.time.Year;
import java.util.Optional;
import java.util.Set;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import ua.foxminded.carrestservice.common.validation.MinYear;

public record CarSearchRequest(

        @Positive
        Long manufacturerId,

        @Positive
        Long modelId,

        @MinYear(1900)
        @PastOrPresent
        Year yearFrom,

        @MinYear(1900)
        @PastOrPresent
        Year yearTo,

        Set<@NotNull @Positive Long> categoryIds

) {

    @AssertTrue(message = "yearFrom must not be greater than yearTo")
    public boolean isYearRangeValid() {
        return Optional.ofNullable(yearFrom)
                .map(from -> yearTo == null || !from.isAfter(yearTo))
                .orElse(true);
    }
}
