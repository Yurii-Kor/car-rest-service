package ua.foxminded.carrestservice.web.carmodel;

import java.net.URI;
import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.foxminded.carrestservice.service.carmodel.CarModelService;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelCreateRequest;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelUpdateRequest;
import ua.foxminded.carrestservice.web.carmodel.dto.CarModelResponse;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;

@Tag(
        name = "Car Models",
        description = "Car model management and selection"
)
@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
public class CarModelController implements CarModelApi {

    private final CarModelService carModelService;

    @Override
    @PostMapping
    public ResponseEntity<IdResponse> create(
            @Valid @RequestBody CarModelCreateRequest request) {

        var id = carModelService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/models/" + id))
                .body(new IdResponse(id));
    }

    @Override
    @GetMapping("/{id}")
    public CarModelResponse findById(@PathVariable Long id) {
        return new CarModelResponse(carModelService.findById(id));
    }

    @Override
    @GetMapping
    public Page<CarModelResponse> findAllByManufacturerId(
            @RequestParam Long manufacturerId,

            @ParameterObject
            @PageableDefault(size = 12, sort = "name") Pageable pageable) {

        return carModelService.findAllByManufacturerId(
                        manufacturerId, pageable)
                .map(CarModelResponse::new);
    }

    @Override
    @GetMapping("/options")
    public List<CarModelResponse> findAllForSelection(
            @RequestParam Long manufacturerId) {

        return carModelService.findAllForSelection(manufacturerId)
                .stream()
                .map(CarModelResponse::new)
                .toList();
    }

    @Override
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable Long id,
            @Valid @RequestBody CarModelUpdateRequest request) {

        carModelService.update(id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        carModelService.delete(id);
    }
}
