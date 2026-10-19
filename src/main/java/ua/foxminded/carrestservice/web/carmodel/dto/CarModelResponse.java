package ua.foxminded.carrestservice.web.carmodel.dto;

import ua.foxminded.carrestservice.model.CarModel;

public record CarModelResponse(
        Long id,
        String name,
        Long manufacturerId
) {

    public CarModelResponse(CarModel carModel) {
        this(
                carModel.getId(),
                carModel.getName(),
                carModel.getManufacturer().getId());
    }
}
