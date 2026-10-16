
package ua.foxminded.carrestservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.foxminded.carrestservice.model.CarModel;

public interface CarModelRepository extends JpaRepository<CarModel, Long> {

}
