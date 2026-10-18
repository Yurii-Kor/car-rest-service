package ua.foxminded.carrestservice.service.car;

import java.time.Year;
import java.util.ArrayList;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import ua.foxminded.carrestservice.model.Car;
import ua.foxminded.carrestservice.model.Category;
import ua.foxminded.carrestservice.repository.CarRepository;
import ua.foxminded.carrestservice.service.car.dto.CarDetails;
import ua.foxminded.carrestservice.service.car.dto.CarSearchRequest;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarQueryService {

    private final CarRepository carRepository;

    public CarDetails findById(Long id) {
        var car = carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Car not found: id=%d".formatted(id)));

        return toDetails(car);
    }

    public Page<CarDetails> findAll(
            CarSearchRequest request,
            Pageable pageable) {

        var specification = Specification.allOf(
                manufacturerModelFilter(request),
                yearRangeFilter(request),
                categoryFilter(request));

        return carRepository.findAll(specification, pageable)
                .map(this::toDetails);
    }

    private Specification<Car> manufacturerModelFilter(
            CarSearchRequest request) {

        var manufacturerId = Optional.ofNullable(request.manufacturerId());
        var modelId = Optional.ofNullable(request.modelId());

        return (root, query, builder) -> {
            if (manufacturerId.isEmpty() && modelId.isEmpty()) {
                return builder.conjunction();
            }

            var model = root.join("model");
            var predicates = new ArrayList<Predicate>();

            manufacturerId.ifPresent(id -> predicates.add(
                    builder.equal(
                            model.get("manufacturer").get("id"), id)));

            modelId.ifPresent(id -> predicates.add(
                    builder.equal(model.get("id"), id)));

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<Car> yearRangeFilter(
            CarSearchRequest request) {

        var yearFrom = Optional.ofNullable(request.yearFrom());
        var yearTo = Optional.ofNullable(request.yearTo());

        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();

            yearFrom.ifPresent(from -> predicates.add(
                    builder.greaterThanOrEqualTo(
                            root.<Year>get("year"), from)));

            yearTo.ifPresent(to -> predicates.add(
                    builder.lessThanOrEqualTo(
                            root.<Year>get("year"), to)));

            return builder.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<Car> categoryFilter(
            CarSearchRequest request) {

        var categoryIds = Optional.ofNullable(request.categoryIds())
                .filter(ids -> !ids.isEmpty());

        return (root, query, builder) ->
                categoryIds.map(ids -> {
                    var subquery = query.subquery(Long.class);
                    var correlatedCar = subquery.correlate(root);
                    var category = correlatedCar.join("categories");

                    subquery.select(
                            builder.countDistinct(category.get("id")));

                    subquery.where(category.get("id").in(ids));

                    return builder.equal(
                            subquery, (long) ids.size());
                }).orElseGet(builder::conjunction);
    }

    private CarDetails toDetails(Car car) {
        return new CarDetails(
                car.getId(),
                car.getYear(),
                car.getModel().getId(),
                car.getModel().getManufacturer().getId(),
                car.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toCollection(TreeSet::new)));
    }
}
