package ua.foxminded.carrestservice.service.car.dto;

import java.time.Year;
import java.util.Set;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import ua.foxminded.carrestservice.common.validation.MinYear;

public record CarUpdateRequest(

        @PastOrPresent
        @MinYear(1900)
        Year year,

        Long modelId,

        Set<@NotNull Long> categoryIds

) {
}
