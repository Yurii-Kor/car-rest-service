package ua.foxminded.carrestservice.web.carmodel;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ua.foxminded.carrestservice.service.carmodel.CarModelService;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelCreateRequest;
import ua.foxminded.carrestservice.service.carmodel.dto.CarModelUpdateRequest;
import ua.foxminded.carrestservice.web.carmodel.dto.CarModelResponse;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;

@RestController
@RequiredArgsConstructor
public class CarModelController implements CarModelApi {

    private final CarModelService carModelService;

    @Override
    public ResponseEntity<IdResponse> create(
            CarModelCreateRequest request) {

        var id = carModelService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/models/" + id))
                .body(new IdResponse(id));
    }

    @Override
    public CarModelResponse findById(Long id) {
        return new CarModelResponse(carModelService.findById(id));
    }

    @Override
    public Page<CarModelResponse> findAllByManufacturerId(
            Long manufacturerId,
            Pageable pageable) {

        return carModelService.findAllByManufacturerId(
                        manufacturerId, pageable)
                .map(CarModelResponse::new);
    }

    @Override
    public List<CarModelResponse> findAllForSelection(
            Long manufacturerId) {

        return carModelService.findAllForSelection(manufacturerId)
                .stream()
                .map(CarModelResponse::new)
                .toList();
    }

    @Override
    public void update(Long id, CarModelUpdateRequest request) {
        carModelService.update(id, request);
    }

    @Override
    public void delete(Long id) {
        carModelService.delete(id);
    }
}
