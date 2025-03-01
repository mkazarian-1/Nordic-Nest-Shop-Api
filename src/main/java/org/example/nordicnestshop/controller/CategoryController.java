package org.example.nordicnestshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.nordicnestshop.dto.category.CategoryDto;
import org.example.nordicnestshop.dto.category.CreateCategoryDto;
import org.example.nordicnestshop.dto.category.UpdateCategoryDto;
import org.example.nordicnestshop.model.Category;
import org.example.nordicnestshop.service.CategoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Category repository manager",
        description = "Endpoints for basic category repository management")
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping()
    //    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create category",
            description = """
                    Return the newly created category if the creation went well.
                    \nNecessary role: ADMIN
                    """)
    public CategoryDto createCategory(@Valid CreateCategoryDto requestDto) {
        return categoryService.create(requestDto);
    }

    @PutMapping("/{id}")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update category",
            description = """
                    Return the update book if the update went well
                    \nNecessary role: ADMIN
                    """)
    public CategoryDto updateCategory(@Valid UpdateCategoryDto requestDto,
                                      @PathVariable Long id) {
        return categoryService.update(requestDto, id);
    }

    @DeleteMapping("/{id}")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete category",
            description = """
                    Return 204 status if delete went well
                    \nNecessary role: ADMIN
                    """)
    public void deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
    }

    @GetMapping("/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Get category by id",
            description = """
                    Returns the category by the specified parameter
                    \nNecessary role: USER
                    """)
    public CategoryDto getCategoryById(@PathVariable Long id) {
        return categoryService.getById(id);
    }

    @GetMapping()
    //    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "Get all categories (with pagination and sorting)",
            description = """
                    Returns list
                    of all available categories
                    by pages and give ability
                    to sort categories according to the specified parameters
                    \nNecessary role: USER
                    """)
    public Page<CategoryDto> getAll(@PageableDefault(size = 5) Pageable pageable) {
        return categoryService.getAll(pageable);
    }

    @GetMapping("/title")
    //@PreAuthorize("hasAuthority('USER')")
    public CategoryDto getByTitle(@RequestParam String title) {
        return categoryService.getByTitle(title);
    }

    @GetMapping("/type")
    //@PreAuthorize("hasAuthority('USER')")
    public Page<CategoryDto> getAllByStatus(@RequestParam Category.CategoryType type,
                                            Pageable pageable) {
        return categoryService.getAllByType(pageable, type);
    }
}
