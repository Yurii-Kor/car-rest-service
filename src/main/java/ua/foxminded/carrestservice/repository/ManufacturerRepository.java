package ua.foxminded.carrestservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ua.foxminded.carrestservice.model.Manufacturer;

public interface ManufacturerRepository extends JpaRepository<Manufacturer, Long> {

    boolean existsByName(String name);
}
