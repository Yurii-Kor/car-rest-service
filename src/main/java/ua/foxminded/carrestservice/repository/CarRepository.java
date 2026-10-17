
package ua.foxminded.carrestservice.repository;

import java.time.Year;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ua.foxminded.carrestservice.model.Car;
import ua.foxminded.carrestservice.repository.projection.CarCategoryRow;
import ua.foxminded.carrestservice.repository.projection.CarRow;

public interface CarRepository extends JpaRepository<Car, Long> {

    boolean existsByModelIdAndYear(Long modelId, Year year);

    @Query("""
            SELECT new ua.foxminded.carrestservice.repository.projection.CarRow(
                car.id,
                car.year,
                model.id,
                manufacturer.id
            )
            FROM Car car
            JOIN car.model model
            JOIN model.manufacturer manufacturer
            WHERE car.id = :id
            """)
    Optional<CarRow> findRowById(@Param("id") Long id);

    @Query(
            value = """
                    SELECT new ua.foxminded.carrestservice.repository.projection.CarRow(
                        car.id,
                        car.year,
                        model.id,
                        manufacturer.id
                    )
                    FROM Car car
                    JOIN car.model model
                    JOIN model.manufacturer manufacturer
                    """,
            countQuery = """
                    SELECT COUNT(car)
                    FROM Car car
                    """
    )
    Page<CarRow> findAllRows(Pageable pageable);

    @Query("""
            SELECT new ua.foxminded.carrestservice.repository.projection.CarCategoryRow(
                car.id,
                category.id
            )
            FROM Car car
            JOIN car.categories category
            WHERE car.id IN :carIds
            """)
    List<CarCategoryRow> findCategoryRowsByCarIds(
            @Param("carIds") Collection<Long> carIds);
}
