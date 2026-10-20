package ua.foxminded.carrestservice.service.carmodel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ua.foxminded.carrestservice.common.normalization.NormalizeSpaces;

public record CarModelCreateRequest(

        @NormalizeSpaces
        @NotBlank
        @Size(max = 255)
        String name,

        @NotNull
        @Positive
        Long manufacturerId

) {
}
