
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
import ua.foxminded.carrestservice.service.carmodel.CarModelService;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelCreateRequest;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelUpdateRequest;
import ua.foxminded.carrestservice.web.carmodel.dto.CarModelResponse;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;

@RestController
@RequestMapping("/api/v1/models")
@RequiredArgsConstructor
public class CarModelController {

    private final CarModelService carModelService;

    @PostMapping
    public ResponseEntity<IdResponse> create(
            @Valid @RequestBody CarModelCreateRequest request) {

        var id = carModelService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/models/" + id))
                .body(new IdResponse(id));
    }

    @GetMapping("/{id}")
    public CarModelResponse findById(@PathVariable Long id) {
        return new CarModelResponse(carModelService.findById(id));
    }

    @GetMapping
    public Page<CarModelResponse> findAllByManufacturerId(
            @RequestParam Long manufacturerId,
            @PageableDefault(size = 12, sort = "name") Pageable pageable) {

        return carModelService.findAllByManufacturerId(
                        manufacturerId, pageable)
                .map(CarModelResponse::new);
    }

    @GetMapping("/options")
    public List<CarModelResponse> findAllForSelection(
            @RequestParam Long manufacturerId) {

        return carModelService.findAllForSelection(manufacturerId)
                .stream()
                .map(CarModelResponse::new)
                .toList();
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable Long id,
            @Valid @RequestBody CarModelUpdateRequest request) {

        carModelService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        carModelService.delete(id);
    }
}
