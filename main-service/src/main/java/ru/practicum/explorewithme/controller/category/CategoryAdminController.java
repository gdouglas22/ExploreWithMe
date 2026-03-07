package ru.practicum.explorewithme.controller.category;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategory;
import ru.practicum.explorewithme.service.category.CategoryService;

@Controller
@RequestMapping("/admin/categories")
@RequiredArgsConstructor
public class CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody NewCategory newCategory) {
        CategoryDto categoryDto = categoryService.createCategoryByAdmin(newCategory);
        return ResponseEntity.ok().body(categoryDto);
    }

    @DeleteMapping("/{catId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long catId) {
        categoryService.deleteCategoryByAdmin(catId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{catId}")
    public ResponseEntity<CategoryDto> updateCategory(@RequestBody NewCategory newCategory,
                                                      @PathVariable Long catId) {
        CategoryDto categoryDto = categoryService.updateCategoryByAdmin(catId, newCategory);
        return ResponseEntity.ok().body(categoryDto);
    }
}
