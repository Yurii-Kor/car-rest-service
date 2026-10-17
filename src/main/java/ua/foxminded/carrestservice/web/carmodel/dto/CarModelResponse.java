package ua.foxminded.carrestservice.web.carmodel.dto;

public record CarModelResponse(
        Long id,
        String name,
        Long manufacturerId
) {
}
