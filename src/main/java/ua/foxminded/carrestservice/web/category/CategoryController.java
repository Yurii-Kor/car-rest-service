package ua.foxminded.carrestservice.web.category;

import java.net.URI;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ua.foxminded.carrestservice.service.category.CategoryService;
import ua.foxminded.carrestservice.service.category.dto.CategoryRequest;
import ua.foxminded.carrestservice.web.category.dto.CategoryResponse;
import ua.foxminded.carrestservice.web.common.dto.IdResponse;

@RestController
@RequiredArgsConstructor
public class CategoryController implements CategoryApi {

    private final CategoryService categoryService;

    @Override
    public ResponseEntity<IdResponse> create(CategoryRequest request) {

        var id = categoryService.create(request);

        return ResponseEntity
                .created(URI.create("/api/v1/categories/" + id))
                .body(new IdResponse(id));
    }

    @Override
    public CategoryResponse findById(Long id) {
        return new CategoryResponse(categoryService.findById(id));
    }

    @Override
    public Page<CategoryResponse> findAll(Pageable pageable) {

        return categoryService.findAll(pageable)
                .map(CategoryResponse::new);
    }

    @Override
    public List<CategoryResponse> findAllForSelection() {

        return categoryService.findAllForSelection()
                .stream()
                .map(CategoryResponse::new)
                .toList();
    }

    @Override
    public void update(Long id, CategoryRequest request) {
        categoryService.update(id, request);
    }

    @Override
    public void delete(Long id) {
        categoryService.delete(id);
    }
}
