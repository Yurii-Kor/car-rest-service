package ua.foxminded.carrestservice.web.manufacturer;

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
import ua.foxminded.carrestservice.service.manufacturer.ManufacturerService;
import ua.foxminded.carrestservice.service.manufacturer.dto.ManufacturerRequest;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;
import ua.foxminded.carrestservice.web.manufacturer.dto.ManufacturerResponse;

@Tag(
        name = "Manufacturers",
        description = "Manufacturer management and selection"
)
@RestController
@RequestMapping("/api/v1/manufacturers")
@RequiredArgsConstructor
public class ManufacturerController implements ManufacturerApi {

    private final ManufacturerService manufacturerService;

    @Override
    @PostMapping
    public ResponseEntity<IdResponse> create(
            @Valid @RequestBody ManufacturerRequest request) {

        var id = manufacturerService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/manufacturers/" + id))
                .body(new IdResponse(id));
    }

    @Override
    @GetMapping("/{id}")
    public ManufacturerResponse findById(@PathVariable Long id) {
        return new ManufacturerResponse(manufacturerService.findById(id));
    }

    @Override
    @GetMapping
    public Page<ManufacturerResponse> findAll(

            @ParameterObject
            @PageableDefault(size = 12, sort = "name") Pageable pageable) {

        return manufacturerService.findAll(pageable)
                .map(ManufacturerResponse::new);
    }

    @Override
    @GetMapping("/options")
    public List<ManufacturerResponse> findAllForSelection() {
        return manufacturerService.findAllForSelection().stream()
                .map(ManufacturerResponse::new)
                .toList();
    }

    @Override
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable Long id,
            @Valid @RequestBody ManufacturerRequest request) {

        manufacturerService.update(id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        manufacturerService.delete(id);
    }
}
