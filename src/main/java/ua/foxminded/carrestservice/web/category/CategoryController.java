package ua.foxminded.carrestservice.web.category;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.foxminded.carrestservice.service.category.CategoryService;
import ua.foxminded.carrestservice.service.category.dto.CategoryRequest;
import ua.foxminded.carrestservice.web.category.dto.CategoryResponse;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;

@Tag(
        name = "Categories",
        description = "Category management and selection"
)
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController implements CategoryApi {

    private final CategoryService categoryService;

    @Override
    @PostMapping
    public ResponseEntity<IdResponse> create(
            @Valid @RequestBody CategoryRequest request) {

        var id = categoryService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/categories/" + id))
                .body(new IdResponse(id));
    }

    @Override
    @GetMapping("/{id}")
    public CategoryResponse findById(@PathVariable Long id) {
        return new CategoryResponse(categoryService.findById(id));
    }

    @Override
    @GetMapping
    public Page<CategoryResponse> findAll(
            @PageableDefault(size = 12, sort = "name") Pageable pageable) {

        return categoryService.findAll(pageable)
                .map(CategoryResponse::new);
    }

    @Override
    @GetMapping("/options")
    public List<CategoryResponse> findAllForSelection() {
        return categoryService.findAllForSelection()
                .stream()
                .map(CategoryResponse::new)
                .toList();
    }

    @Override
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {

        categoryService.update(id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
