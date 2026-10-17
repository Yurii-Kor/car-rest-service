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
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelRequest;
import ua.foxminded.carrestservice.service.manufacturer.ManufacturerService;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CarModelService {

    private final CarModelRepository carModelRepository;
    private final ManufacturerService manufacturerService;

    @Transactional
    public Long create(Long manufacturerId, CarModelRequest request) {
        var manufacturer = manufacturerService.findById(manufacturerId);

        checkNameAvailable(manufacturerId, request.name());

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

        manufacturerService.findById(manufacturerId);

        return carModelRepository.findAllByManufacturerId(
                manufacturerId, pageable);
    }

    public List<CarModel> findAllForSelection(Long manufacturerId) {
        manufacturerService.findById(manufacturerId);

        return carModelRepository.findAllByManufacturerId(
                manufacturerId, Sort.by("name"));
    }

    @Transactional
    public void update(Long id, CarModelRequest request) {
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
        carModelRepository.delete(findById(id));

        log.info("Car model deletion requested: id={}", id);
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
