package ua.foxminded.carrestservice.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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


    @Query("""
            SELECT cm.manufacturer.id
            FROM CarModel cm
            WHERE cm.id = :modelId
            """)
    Optional<Long> findManufacturerIdById(@Param("modelId") Long modelId);

}
