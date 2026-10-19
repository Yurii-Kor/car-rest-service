package ua.foxminded.carrestservice.service.car.dto;

import java.time.Year;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import ua.foxminded.carrestservice.model.Car;
import ua.foxminded.carrestservice.model.Category;

public record CarDetails(
        Long id,
        Year year,
        Long modelId,
        Long manufacturerId,
        Set<Long> categoryIds
) {

    public CarDetails(Car car) {
        this(
                car.getId(),
                car.getYear(),
                car.getModel().getId(),
                car.getModel().getManufacturer().getId(),
                car.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toCollection(TreeSet::new)));
    }
}
