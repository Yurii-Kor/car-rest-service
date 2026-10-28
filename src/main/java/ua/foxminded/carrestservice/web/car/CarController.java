package ua.foxminded.carrestservice.web.car;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ua.foxminded.carrestservice.service.car.CarQueryService;
import ua.foxminded.carrestservice.service.car.CarService;
import ua.foxminded.carrestservice.service.car.dto.CarCreateRequest;
import ua.foxminded.carrestservice.service.car.dto.CarDetails;
import ua.foxminded.carrestservice.service.car.dto.CarSearchRequest;
import ua.foxminded.carrestservice.service.car.dto.CarUpdateRequest;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;

@RestController
@RequiredArgsConstructor
public class CarController implements CarApi {

    private final CarService carService;
    private final CarQueryService carQueryService;

    @Override
    public ResponseEntity<IdResponse> create(CarCreateRequest request) {

        var id = carService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/cars/" + id))
                .body(new IdResponse(id));
    }

    @Override
    public CarDetails findById(Long id) {
        return carQueryService.findById(id);
    }

    @Override
    public Page<CarDetails> findAll(
            CarSearchRequest request,
            Pageable pageable) {

        return carQueryService.findAll(request, pageable);
    }

    @Override
    public void update(Long id, CarUpdateRequest request) {
        carService.update(id, request);
    }

    @Override
    public void delete(Long id) {
        carService.delete(id);
    }
}
