package ua.foxminded.carrestservice.web.carmodel;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.foxminded.carrestservice.model.CarModel;
import ua.foxminded.carrestservice.service.carmodel.CarModelService;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelRequest;
import ua.foxminded.carrestservice.web.carmodel.dto.CarModelResponse;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CarModelController {

    private final CarModelService carModelService;

    @PostMapping("/manufacturers/{manufacturerId}/models")
    public ResponseEntity<Void> create(
            @PathVariable Long manufacturerId,
            @Valid @RequestBody CarModelRequest request) {

        var id = carModelService.create(manufacturerId, request);

        return ResponseEntity
                .created(URI.create("/api/v1/models/" + id))
                .build();
    }

    @GetMapping("/models/{id}")
    public CarModelResponse findById(@PathVariable Long id) {
        return toResponse(carModelService.findById(id));
    }

    @GetMapping("/manufacturers/{manufacturerId}/models")
    public Page<CarModelResponse> findAllByManufacturerId(
            @PathVariable Long manufacturerId,
            @PageableDefault(size = 12, sort = "name") Pageable pageable) {

        return carModelService.findAllByManufacturerId(
                        manufacturerId, pageable)
                .map(this::toResponse);
    }

    @GetMapping("/manufacturers/{manufacturerId}/models/options")
    public List<CarModelResponse> findAllForSelection(
            @PathVariable Long manufacturerId) {

        return carModelService.findAllForSelection(manufacturerId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @PutMapping("/models/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable Long id,
            @Valid @RequestBody CarModelRequest request) {

        carModelService.update(id, request);
    }

    @DeleteMapping("/models/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        carModelService.delete(id);
    }

    private CarModelResponse toResponse(CarModel carModel) {
        return new CarModelResponse(
                carModel.getId(),
                carModel.getName(),
                carModel.getManufacturer().getId());
    }
}
