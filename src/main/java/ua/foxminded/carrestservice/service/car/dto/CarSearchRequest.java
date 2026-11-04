package ua.foxminded.carrestservice.service.car.dto;

import java.time.Year;
import java.util.Optional;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
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

        @Schema(
                implementation = Integer.class,
                description = "Start of manufacturing year range",
                minimum = "1900",
                example = "2010"
        )
        @MinYear(1900)
        @PastOrPresent
        Year yearFrom,

        @Schema(
                implementation = Integer.class,
                description = "End of manufacturing year range",
                minimum = "1900",
                example = "2020"
        )
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
