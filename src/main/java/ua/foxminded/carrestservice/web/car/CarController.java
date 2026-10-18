package ua.foxminded.carrestservice.web.car;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.foxminded.carrestservice.service.car.CarQueryService;
import ua.foxminded.carrestservice.service.car.CarService;
import ua.foxminded.carrestservice.service.car.dto.CarDetails;
import ua.foxminded.carrestservice.service.car.dto.CarRequest;
import ua.foxminded.carrestservice.service.car.dto.CarSearchRequest;
import ua.foxminded.carrestservice.service.car.dto.CarUpdateRequest;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;
    private final CarQueryService carQueryService;

    @PostMapping
    public ResponseEntity<Void> create(
            @Valid @RequestBody CarRequest request) {

        var id = carService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/cars/" + id))
                .build();
    }

    @GetMapping("/{id}")
    public CarDetails findById(@PathVariable Long id) {
        return carQueryService.findById(id);
    }

    @GetMapping
    public Page<CarDetails> findAll(
            @Valid @ModelAttribute CarSearchRequest request,
            @PageableDefault(size = 12, sort = "id") Pageable pageable) {

        return carQueryService.findAll(request, pageable);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable Long id,
            @Valid @RequestBody CarUpdateRequest request) {

        carService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        carService.delete(id);
    }
}
