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
import ua.foxminded.carrestservice.model.Category;
import ua.foxminded.carrestservice.repository.CarModelRepository;
import ua.foxminded.carrestservice.repository.CarRepository;
import ua.foxminded.carrestservice.repository.CategoryRepository;
import ua.foxminded.carrestservice.service.car.dto.CarCreateRequest;
import ua.foxminded.carrestservice.service.car.dto.CarUpdateRequest;
import ua.foxminded.carrestservice.service.car.exception.CarManufacturerChangeNotAllowedException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CarService {

    private final CarRepository carRepository;
    private final CarModelRepository carModelRepository;
    private final CategoryRepository categoryRepository;

    public Long create(CarCreateRequest request) {
        checkModelExists(request.modelId());
        checkCombinationAvailable(request.modelId(), request.year());

        var categories = resolveCategories(request.categoryIds());
        var model = carModelRepository.getReferenceById(request.modelId());

        var car = new Car(request.year(), model);
        car.getCategories().addAll(categories);

        carRepository.save(car);

        log.info("Car created: id={}", car.getId());

        return car.getId();
    }

    public void update(Long id, CarUpdateRequest patch) {
        var car = findEntityById(id);

        if (!hasChanges(car, patch)) {
            return;
        }

        validateModelPatch(car, patch);
        validateCombinationPatch(car, patch);

        applyCategoriesPatch(car, patch);
        applyYearPatch(car, patch);
        applyModelPatch(car, patch);

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

    private boolean hasChanges(Car car, CarUpdateRequest patch) {
        return isYearChanged(car, patch.year())
                || isModelChanged(car, patch.modelId())
                || isCategoriesChanged(car, patch.categoryIds());
    }

    private boolean isYearChanged(Car car, Year year) {
        return Optional.ofNullable(year)
                .filter(value -> !value.equals(car.getYear()))
                .isPresent();
    }

    private boolean isModelChanged(Car car, Long modelId) {
        return Optional.ofNullable(modelId)
                .filter(id -> !id.equals(car.getModel().getId()))
                .isPresent();
    }

    private boolean isCategoriesChanged(
            Car car, Set<Long> categoryIds) {

        return Optional.ofNullable(categoryIds)
                .filter(ids -> !ids.equals(currentCategoryIds(car)))
                .isPresent();
    }

    private void validateModelPatch(Car car, CarUpdateRequest patch) {
        Optional.ofNullable(patch.modelId())
                .filter(modelId -> isModelChanged(car, modelId))
                .ifPresent(modelId ->
                        checkManufacturerChangeAllowed(car, modelId));
    }

    private void validateCombinationPatch(
            Car car, CarUpdateRequest patch) {

        if (!isYearChanged(car, patch.year())
                && !isModelChanged(car, patch.modelId())) {
            return;
        }

        var targetYear = Optional.ofNullable(patch.year())
                .orElseGet(car::getYear);

        var targetModelId = Optional.ofNullable(patch.modelId())
                .orElseGet(() -> car.getModel().getId());

        checkCombinationAvailable(targetModelId, targetYear);
    }

    private void checkManufacturerChangeAllowed(Car car, Long modelId) {
        var requestedManufacturerId = carModelRepository
                .findManufacturerIdById(modelId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Car model not found: id=%d".formatted(modelId)));

        var currentManufacturerId = car.getModel()
                .getManufacturer()
                .getId();

        if (!currentManufacturerId.equals(requestedManufacturerId)) {
            throw new CarManufacturerChangeNotAllowedException(
                    car.getId(),
                    currentManufacturerId,
                    requestedManufacturerId);
        }
    }

    private void applyYearPatch(Car car, CarUpdateRequest patch) {
        Optional.ofNullable(patch.year())
                .filter(year -> isYearChanged(car, year))
                .ifPresent(car::setYear);
    }

    private void applyModelPatch(Car car, CarUpdateRequest patch) {
        Optional.ofNullable(patch.modelId())
                .filter(modelId -> isModelChanged(car, modelId))
                .map(carModelRepository::getReferenceById)
                .ifPresent(car::setModel);
    }

    private void applyCategoriesPatch(Car car, CarUpdateRequest patch) {
        Optional.ofNullable(patch.categoryIds())
                .filter(ids -> isCategoriesChanged(car, ids))
                .map(this::resolveCategories)
                .ifPresent(categories ->
                        updateCategories(car, categories));
    }

    private void updateCategories(Car car, Set<Category> categories) {
        car.getCategories().clear();
        car.getCategories().addAll(categories);
    }

    private void checkModelExists(Long modelId) {
        if (!carModelRepository.existsById(modelId)) {
            throw new EntityNotFoundException(
                    "Car model not found: id=%d".formatted(modelId));
        }
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
