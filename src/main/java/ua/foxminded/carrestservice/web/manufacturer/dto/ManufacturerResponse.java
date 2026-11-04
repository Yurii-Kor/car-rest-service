
package ua.foxminded.carrestservice.web.manufacturer.dto;

import ua.foxminded.carrestservice.model.Manufacturer;

public record ManufacturerResponse(
        Long id,
        String name
) {

    public ManufacturerResponse(Manufacturer manufacturer) {
        this(
                manufacturer.getId(),
                manufacturer.getName());
    }
}
