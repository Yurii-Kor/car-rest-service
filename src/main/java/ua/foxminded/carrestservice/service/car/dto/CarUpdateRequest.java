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

public record CarUpdateRequest(

        @Schema(
                implementation = Integer.class,
                description = "Manufacturing year",
                minimum = "1900",
                example = "2020"
        )
        @PastOrPresent
        @MinYear(1900)
        Year year,

        @Positive
        Long modelId,

        Set<@NotNull @Positive Long> categoryIds

) {

    @Schema(hidden = true)
    @AssertTrue(message = "At least one field must be provided")
    public boolean isAnyFieldProvided() {

        return Optional.ofNullable(year).isPresent()
                || Optional.ofNullable(modelId).isPresent()
                || Optional.ofNullable(categoryIds).isPresent();
    }
}
