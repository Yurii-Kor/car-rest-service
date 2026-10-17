package ua.foxminded.carrestservice.web.category;

import org.springframework.test.util.ReflectionTestUtils;

import lombok.experimental.UtilityClass;
import ua.foxminded.carrestservice.model.Category;

@UtilityClass
class CategoryControllerTestFactory {

    Category category(Long id, String name) {
        var category = new Category(name);

        ReflectionTestUtils.setField(category, "id", id);

        return category;
    }
}
