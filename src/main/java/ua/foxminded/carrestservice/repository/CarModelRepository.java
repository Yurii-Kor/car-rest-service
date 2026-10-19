package ua.foxminded.carrestservice.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.foxminded.carrestservice.model.CarModel;

public interface CarModelRepository extends JpaRepository<CarModel, Long> {

    boolean existsByManufacturerIdAndName(
            Long manufacturerId,
            String name);

    Page<CarModel> findAllByManufacturerId(
            Long manufacturerId,
            Pageable pageable);

    List<CarModel> findAllByManufacturerId(
            Long manufacturerId,
            Sort sort);
}
