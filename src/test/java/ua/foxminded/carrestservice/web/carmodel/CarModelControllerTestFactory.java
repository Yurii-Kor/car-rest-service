package ua.foxminded.carrestservice.web.carmodel;

import org.springframework.test.util.ReflectionTestUtils;

import lombok.experimental.UtilityClass;
import ua.foxminded.carrestservice.model.CarModel;
import ua.foxminded.carrestservice.model.Manufacturer;

@UtilityClass
class CarModelControllerTestFactory {

    CarModel carModel(
            Long modelId,
            String modelName,
            Long manufacturerId,
            String manufacturerName) {

        var manufacturer = new Manufacturer(manufacturerName);
        ReflectionTestUtils.setField(manufacturer, "id", manufacturerId);

        var carModel = new CarModel(modelName, manufacturer);
        ReflectionTestUtils.setField(carModel, "id", modelId);

        return carModel;
    }
}
