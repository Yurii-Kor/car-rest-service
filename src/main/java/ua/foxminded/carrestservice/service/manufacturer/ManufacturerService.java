package ua.foxminded.carrestservice.service.manufacturer;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.foxminded.carrestservice.model.Manufacturer;
import ua.foxminded.carrestservice.repository.CarRepository;
import ua.foxminded.carrestservice.repository.ManufacturerRepository;
import ua.foxminded.carrestservice.service.exception.EntityInUseException;
import ua.foxminded.carrestservice.service.manufacturer.dto.ManufacturerRequest;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ManufacturerService {

    private final ManufacturerRepository manufacturerRepository;
    private final CarRepository carRepository;

    @Transactional
    public Long create(ManufacturerRequest request) {
        checkNameAvailable(request.name());

        var manufacturer = manufacturerRepository.save(
                new Manufacturer(request.name()));

        log.info("Manufacturer created: id={}", manufacturer.getId());

        return manufacturer.getId();
    }

    public Manufacturer findById(Long id) {
        return manufacturerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Manufacturer not found: id=%d".formatted(id)));
    }

    public Page<Manufacturer> findAll(Pageable pageable) {
        return manufacturerRepository.findAll(pageable);
    }

    public List<Manufacturer> findAllForSelection() {
        return manufacturerRepository.findAll(Sort.by("name"));
    }

    @Transactional
    public void update(Long id, ManufacturerRequest request) {
        var manufacturer = findById(id);

        if (!manufacturer.getName().equals(request.name())) {
            checkNameAvailable(request.name());
            manufacturer.setName(request.name());

            log.info("Manufacturer updated: id={}", id);
        }
    }

    @Transactional
    public void delete(Long id) {
        var manufacturer = findById(id);
    
        if (carRepository.existsByModel_Manufacturer_Id(id)) {
            throw new EntityInUseException(
                    "Cannot delete manufacturer with existing cars: id=%d"
                            .formatted(id));
        }
    
        manufacturerRepository.delete(manufacturer);
    
        log.info("Manufacturer deleted: id={}", id);
    }

    private void checkNameAvailable(String name) {
        if (manufacturerRepository.existsByName(name)) {
            throw new EntityExistsException(
                    "Manufacturer already exists: %s".formatted(name));
        }
    }
}
