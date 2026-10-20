package ua.foxminded.carrestservice.service.carmodel;

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
import ua.foxminded.carrestservice.model.CarModel;
import ua.foxminded.carrestservice.repository.CarModelRepository;
import ua.foxminded.carrestservice.repository.CarRepository;
import ua.foxminded.carrestservice.repository.ManufacturerRepository;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelCreateRequest;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelUpdateRequest;
import ua.foxminded.carrestservice.service.exception.EntityInUseException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarModelService {

    private final CarModelRepository carModelRepository;
    private final CarRepository carRepository;
    private final ManufacturerRepository manufacturerRepository;

    @Transactional
    public Long create(CarModelCreateRequest request) {
        var manufacturerId = request.manufacturerId();

        checkManufacturerExists(manufacturerId);
        checkNameAvailable(manufacturerId, request.name());

        var manufacturer = manufacturerRepository.getReferenceById(manufacturerId);

        var carModel = carModelRepository.save(
                new CarModel(request.name(), manufacturer));

        log.info("Car model created: id={}", carModel.getId());

        return carModel.getId();
    }

    public CarModel findById(Long id) {
        return carModelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Car model not found: id=%d".formatted(id)));
    }

    public Page<CarModel> findAllByManufacturerId(
            Long manufacturerId, Pageable pageable) {

        checkManufacturerExists(manufacturerId);

        return carModelRepository.findAllByManufacturerId(
                manufacturerId, pageable);
    }

    public List<CarModel> findAllForSelection(Long manufacturerId) {
        checkManufacturerExists(manufacturerId);

        return carModelRepository.findAllByManufacturerId(
                manufacturerId, Sort.by("name"));
    }

    @Transactional
    public void update(Long id, CarModelUpdateRequest request) {
        var carModel = findById(id);

        if (!carModel.getName().equals(request.name())) {
            var manufacturerId = carModel.getManufacturer().getId();

            checkNameAvailable(manufacturerId, request.name());
            carModel.setName(request.name());

            log.info("Car model updated: id={}", id);
        }
    }

    @Transactional
    public void delete(Long id) {
        var carModel = findById(id);

        if (carRepository.existsByModelId(id)) {
            throw new EntityInUseException(
                    "Cannot delete car model with existing cars: id=%d"
                            .formatted(id));
        }

        carModelRepository.delete(carModel);

        log.info("Car model deleted: id={}", id);
    }

    private void checkManufacturerExists(Long manufacturerId) {
        if (!manufacturerRepository.existsById(manufacturerId)) {
            throw new EntityNotFoundException(
                    "Manufacturer not found: id=%d".formatted(manufacturerId));
        }
    }

    private void checkNameAvailable(Long manufacturerId, String name) {
        if (carModelRepository.existsByManufacturerIdAndName(
                manufacturerId, name)) {

            throw new EntityExistsException(
                    "Car model already exists: manufacturerId=%d, name=%s"
                            .formatted(manufacturerId, name));
        }
    }
}
