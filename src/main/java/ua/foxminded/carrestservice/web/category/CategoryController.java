package ua.foxminded.carrestservice.web.category;

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
import ua.foxminded.carrestservice.service.category.CategoryService;
import ua.foxminded.carrestservice.service.category.dto.CategoryRequest;
import ua.foxminded.carrestservice.web.category.dto.CategoryResponse;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<Void> create(
            @Valid @RequestBody CategoryRequest request) {

        var id = categoryService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/categories/" + id))
                .build();
    }

    @GetMapping("/{id}")
    public CategoryResponse findById(@PathVariable Long id) {
        return new CategoryResponse(categoryService.findById(id));
    }

    @GetMapping
    public Page<CategoryResponse> findAll(
            @PageableDefault(size = 12, sort = "name") Pageable pageable) {

        return categoryService.findAll(pageable)
                .map(CategoryResponse::new);
    }

    @GetMapping("/options")
    public List<CategoryResponse> findAllForSelection() {
        return categoryService.findAllForSelection()
                .stream()
                .map(CategoryResponse::new)
                .toList();
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        categoryService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
