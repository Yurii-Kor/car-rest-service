package ua.foxminded.carrestservice.repository;

import java.time.Year;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import ua.foxminded.carrestservice.model.Car;

public interface CarRepository
        extends JpaRepository<Car, Long>, JpaSpecificationExecutor<Car> {

    boolean existsByModelId(Long modelId);

    boolean existsByCategories_Id(Long categoryId);

    boolean existsByModel_Manufacturer_Id(Long manufacturerId);

    boolean existsByModelIdAndYear(Long modelId, Year year);

    @Override
    @EntityGraph(attributePaths = {
            "model",
            "model.manufacturer",
            "categories"
    })
    Page<Car> findAll(Specification<Car> specification, Pageable pageable);
}
