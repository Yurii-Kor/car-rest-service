package ua.foxminded.carrestservice.service.car.dto;

import java.time.Year;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import ua.foxminded.carrestservice.common.validation.MinYear;

public record CarCreateRequest(

        @Schema(
                implementation = Integer.class,
                description = "Manufacturing year",
                minimum = "1900",
                example = "2020"
        )
        @NotNull
        @PastOrPresent
        @MinYear(1900)
        Year year,

        @NotNull
        @Positive
        Long modelId,

        @NotNull
        Set<@NotNull @Positive Long> categoryIds

) {
}