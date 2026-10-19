package ua.foxminded.carrestservice.service.car.dto;

import java.time.Year;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import ua.foxminded.carrestservice.common.validation.MinYear;

public record CarUpdateRequest(

        @PastOrPresent
        @MinYear(1900)
        Year year,

        @Positive
        Long modelId,

        Set<@NotNull @Positive Long> categoryIds

) {
}
