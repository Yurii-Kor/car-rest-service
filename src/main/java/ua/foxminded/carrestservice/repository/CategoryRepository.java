package ua.foxminded.carrestservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.foxminded.carrestservice.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByName(String name);
}
