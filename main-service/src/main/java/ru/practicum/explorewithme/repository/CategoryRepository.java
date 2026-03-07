package ru.practicum.explorewithme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.explorewithme.model.user.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
