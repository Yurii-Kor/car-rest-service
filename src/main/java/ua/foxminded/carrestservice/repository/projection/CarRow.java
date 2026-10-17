package ua.foxminded.carrestservice.repository.projection;

import java.time.Year;

public record CarRow(
        Long id,
        Year year,
        Long modelId,
        Long manufacturerId
) {
}
