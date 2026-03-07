package ru.practicum.explorewithme.service.category;

import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategory;

public interface CategoryService {
    CategoryDto createCategoryByAdmin(NewCategory newCategory);

    void deleteCategoryByAdmin(Long catId);

    CategoryDto updateCategoryByAdmin(Long catId, NewCategory newCategory);
}
