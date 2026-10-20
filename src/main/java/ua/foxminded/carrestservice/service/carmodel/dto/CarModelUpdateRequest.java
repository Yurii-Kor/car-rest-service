package ua.foxminded.carrestservice.service.carmodel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ua.foxminded.carrestservice.common.normalization.NormalizeSpaces;

public record CarModelUpdateRequest(

        @NormalizeSpaces
        @NotBlank
        @Size(max = 255)
        String name

) {
}
