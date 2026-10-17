package ua.foxminded.carrestservice.service.car.dto;

import java.time.Year;
import java.util.Set;

public record CarDetails(
        Long id,
        Year year,
        Long modelId,
        Long manufacturerId,
        Set<Long> categoryIds
) {
}
