package ua.foxminded.carrestservice.web.car.dto;

import java.util.Set;

public record CarResponse(
        Long id,
        int year,
        Long modelId,
        Long manufacturerId,
        Set<Long> categoryIds
) {
}
