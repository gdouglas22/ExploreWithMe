package ru.practicum.explorewithme.controller.category;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.category.NewCategory;
import ru.practicum.explorewithme.service.category.CategoryService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class CategoriesAdminController {
    private final CategoryService categoryService;

    public ResponseEntity<CategoryDto> createCategory(@RequestBody NewCategory newCategory){

    }
}
