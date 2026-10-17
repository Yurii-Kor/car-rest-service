package ua.foxminded.carrestservice.web.manufacturer;

import org.springframework.test.util.ReflectionTestUtils;

import lombok.experimental.UtilityClass;
import ua.foxminded.carrestservice.model.Manufacturer;

@UtilityClass
class ManufacturerControllerTestFactory {

    Manufacturer manufacturer(Long id, String name) {
        var manufacturer = new Manufacturer(name);

        ReflectionTestUtils.setField(manufacturer, "id", id);

        return manufacturer;
    }
}
