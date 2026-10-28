package ua.foxminded.carrestservice.web.manufacturer;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ua.foxminded.carrestservice.service.manufacturer.ManufacturerService;
import ua.foxminded.carrestservice.service.manufacturer.dto.ManufacturerRequest;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;
import ua.foxminded.carrestservice.web.manufacturer.dto.ManufacturerResponse;

@RestController
@RequiredArgsConstructor
public class ManufacturerController implements ManufacturerApi {

    private final ManufacturerService manufacturerService;

    @Override
    public ResponseEntity<IdResponse> create(ManufacturerRequest request) {

        var id = manufacturerService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/manufacturers/" + id))
                .body(new IdResponse(id));
    }

    @Override
    public ManufacturerResponse findById(Long id) {
        return new ManufacturerResponse(manufacturerService.findById(id));
    }

    @Override
    public Page<ManufacturerResponse> findAll(Pageable pageable) {

        return manufacturerService.findAll(pageable)
                .map(ManufacturerResponse::new);
    }

    @Override
    public List<ManufacturerResponse> findAllForSelection() {

        return manufacturerService.findAllForSelection().stream()
                .map(ManufacturerResponse::new)
                .toList();
    }

    @Override
    public void update(Long id, ManufacturerRequest request) {
        manufacturerService.update(id, request);
    }

    @Override
    public void delete(Long id) {
        manufacturerService.delete(id);
    }
}
