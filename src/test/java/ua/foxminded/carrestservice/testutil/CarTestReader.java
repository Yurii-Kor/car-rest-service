
package ua.foxminded.carrestservice.testutil;

import java.util.stream.Collectors;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ua.foxminded.carrestservice.model.Category;
import ua.foxminded.carrestservice.repository.CarRepository;
import ua.foxminded.carrestservice.service.car.dto.CarDetails;

@TestComponent
@RequiredArgsConstructor
public class CarTestReader {

    private final CarRepository carRepository;

    @Transactional(readOnly = true)
    public CarDetails findById(Long id) {
        var car = carRepository.findById(id).orElseThrow();

        return new CarDetails(
                car.getId(),
                car.getYear(),
                car.getModel().getId(),
                car.getModel().getManufacturer().getId(),
                car.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet()));
    }
}
