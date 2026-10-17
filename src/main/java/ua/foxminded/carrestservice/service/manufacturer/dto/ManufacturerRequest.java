package ua.foxminded.carrestservice.service.manufacturer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ua.foxminded.carrestservice.common.normalization.NormalizeSpaces;

public record ManufacturerRequest(

        @NormalizeSpaces
        @NotBlank
        @Size(max = 255)
        String name

) {
}
