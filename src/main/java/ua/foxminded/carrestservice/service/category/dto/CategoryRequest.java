
package ua.foxminded.carrestservice.service.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ua.foxminded.carrestservice.common.normalization.NormalizeSpaces;

public record CategoryRequest(

        @NormalizeSpaces
        @NotBlank
        @Size(max = 255)
        String name

) {
}
