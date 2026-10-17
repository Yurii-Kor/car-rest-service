package ua.foxminded.carrestservice.web.car;

import java.time.Year;
import java.util.Set;

import lombok.experimental.UtilityClass;
import ua.foxminded.carrestservice.service.car.dto.CarDetails;

@UtilityClass
class CarControllerTestFactory {

    CarDetails carDetails(
            Long id,
            Year year,
            Long modelId,
            Long manufacturerId,
            Long... categoryIds) {

        return new CarDetails(
                id,
                year,
                modelId,
                manufacturerId,
                Set.of(categoryIds));
    }
}
