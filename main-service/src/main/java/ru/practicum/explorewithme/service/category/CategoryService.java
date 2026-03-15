package ru.practicum.explorewithme.service.category;

import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategory;
import ru.practicum.explorewithme.model.category.Category;

import java.util.List;

public interface CategoryService {
    CategoryDto createCategoryByAdmin(NewCategory newCategory);

    void deleteCategoryByAdmin(Long catId);

    CategoryDto updateCategoryByAdmin(Long catId, NewCategory newCategory);

    List<CategoryDto> getCategories(int from, int size);

    CategoryDto getCategoryById(Long catId);

    CategoryDto getCateGoryById(Long id);
}
