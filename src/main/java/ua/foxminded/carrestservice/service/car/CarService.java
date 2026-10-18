package ua.foxminded.carrestservice.service.car;

import java.time.Year;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.foxminded.carrestservice.model.Car;
import ua.foxminded.carrestservice.model.CarModel;
import ua.foxminded.carrestservice.model.Category;
import ua.foxminded.carrestservice.repository.CarRepository;
import ua.foxminded.carrestservice.repository.CategoryRepository;
import ua.foxminded.carrestservice.service.car.dto.CarRequest;
import ua.foxminded.carrestservice.service.car.dto.CarUpdateRequest;
import ua.foxminded.carrestservice.service.car.exception.CarManufacturerChangeNotAllowedException;
import ua.foxminded.carrestservice.service.carmodel.CarModelService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CarService {

    private final CarRepository carRepository;
    private final CarModelService carModelService;
    private final CategoryRepository categoryRepository;

    public Long create(CarRequest request) {
        var model = carModelService.findById(request.modelId());

        checkCombinationAvailable(request.modelId(), request.year());

        var categories = resolveCategories(request.categoryIds());

        var car = new Car(request.year(), model);
        car.getCategories().addAll(categories);

        carRepository.save(car);

        log.info("Car created: id={}", car.getId());

        return car.getId();
    }

    public void update(Long id, CarUpdateRequest request) {
        var car = findEntityById(id);

        var yearUpdate = getYear(car, request);
        var modelUpdate = getModel(car, request);

        if (yearUpdate.isPresent() || modelUpdate.isPresent()) {
            var targetYear = yearUpdate.orElseGet(car::getYear);

            var targetModelId = modelUpdate
                    .map(CarModel::getId)
                    .orElseGet(() -> car.getModel().getId());

            checkCombinationAvailable(targetModelId, targetYear);
        }

        var categoriesUpdate = getCategories(car, request);

        if (yearUpdate.isEmpty()
                && modelUpdate.isEmpty()
                && categoriesUpdate.isEmpty()) {
            return;
        }

        yearUpdate.ifPresent(year -> car.setYear(year));
        modelUpdate.ifPresent(model -> car.setModel(model));
        categoriesUpdate.ifPresent(categories ->
                updateCategories(car, categories));

        log.info("Car updated: id={}", id);
    }

    public void delete(Long id) {
        carRepository.delete(findEntityById(id));

        log.info("Car deletion requested: id={}", id);
    }

    private Car findEntityById(Long id) {
        return carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Car not found: id=%d".formatted(id)));
    }

    private Optional<Year> getYear(Car car, CarUpdateRequest request) {
        return Optional.ofNullable(request.year())
                .filter(year -> !year.equals(car.getYear()));
    }

    private Optional<CarModel> getModel(Car car, CarUpdateRequest request) {
        return Optional.ofNullable(request.modelId())
                .filter(modelId -> !modelId.equals(car.getModel().getId()))
                .map(modelId -> resolveModelForUpdate(car, modelId));
    }

    private Optional<Set<Category>> getCategories(
            Car car, CarUpdateRequest request) {

        return Optional.ofNullable(request.categoryIds())
                .filter(ids -> !ids.equals(currentCategoryIds(car)))
                .map(this::resolveCategories);
    }

    private void updateCategories(Car car, Set<Category> categories) {
        car.getCategories().clear();
        car.getCategories().addAll(categories);
    }

    private CarModel resolveModelForUpdate(Car car, Long modelId) {
        var model = carModelService.findById(modelId);

        var currentManufacturerId = car.getModel()
                .getManufacturer()
                .getId();

        var requestedManufacturerId = model.getManufacturer().getId();

        if (!currentManufacturerId.equals(requestedManufacturerId)) {
            throw new CarManufacturerChangeNotAllowedException(
                    car.getId(),
                    currentManufacturerId,
                    requestedManufacturerId);
        }

        return model;
    }

    private void checkCombinationAvailable(Long modelId, Year year) {
        if (carRepository.existsByModelIdAndYear(modelId, year)) {
            throw new EntityExistsException(
                    "Car already exists: modelId=%d, year=%d"
                            .formatted(modelId, year.getValue()));
        }
    }

    private Set<Long> currentCategoryIds(Car car) {
        return car.getCategories()
                .stream()
                .map(Category::getId)
                .collect(Collectors.toSet());
    }

    private Set<Category> resolveCategories(Set<Long> categoryIds) {
        if (categoryIds.isEmpty()) {
            return Set.of();
        }

        var categories = categoryRepository.findAllById(categoryIds);

        var foundIds = categories.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());

        var missingId = categoryIds.stream()
                .filter(id -> !foundIds.contains(id))
                .findFirst();

        if (missingId.isPresent()) {
            throw new EntityNotFoundException(
                    "Category not found: id=%d".formatted(missingId.get()));
        }

        return new HashSet<>(categories);
    }
}
