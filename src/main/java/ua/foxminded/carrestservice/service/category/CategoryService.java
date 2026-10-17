
package ua.foxminded.carrestservice.service.category;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ua.foxminded.carrestservice.model.Category;
import ua.foxminded.carrestservice.repository.CategoryRepository;
import ua.foxminded.carrestservice.service.category.dto.CategoryRequest;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public Long create(CategoryRequest request) {
        checkNameAvailable(request.name());

        var category = categoryRepository.save(
                new Category(request.name()));

        log.info("Category created: id={}", category.getId());

        return category.getId();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Category not found: id=%d".formatted(id)));
    }

    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    public List<Category> findAllForSelection() {
        return categoryRepository.findAll(Sort.by("name"));
    }

    @Transactional
    public void update(Long id, CategoryRequest request) {
        var category = findById(id);

        if (!category.getName().equals(request.name())) {
            checkNameAvailable(request.name());
            category.setName(request.name());

            log.info("Category updated: id={}", id);
        }
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.delete(findById(id));

        log.info("Category deletion requested: id={}", id);
    }

    private void checkNameAvailable(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new EntityExistsException(
                    "Category already exists: %s".formatted(name));
        }
    }
}
