package ua.foxminded.carrestservice.web.category.dto;

import ua.foxminded.carrestservice.model.Category;

public record CategoryResponse(
        Long id,
        String name
) {

    public CategoryResponse(Category category) {
        this(
                category.getId(),
                category.getName());
    }
}
