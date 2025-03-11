package org.example.nordicnestshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Category Management",
        description = """
                Endpoints for managing categories,
                including creation, updates, deletion, and retrieval.
                """)
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(
            summary = "Create a new category",
            description = """
                    Creates a new category and returns the created category details.
                    \nNecessary role: **ADMIN**
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Category successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(
            @Parameter(description = "Category creation request data", required = true)
            @Valid CreateCategoryDto requestDto) {
        return categoryService.create(requestDto);
    }

    @Operation(
            summary = "Update an existing category",
            description = """
                    Updates the category with the given ID
                    and returns the updated category details.
                    \nNecessary role: **ADMIN**
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category successfully updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public CategoryDto updateCategory(
            @Parameter(description = "Updated category data", required = true)
            @Valid UpdateCategoryDto requestDto,
            @Parameter(description = "Category ID", required = true, example = "1")
            @PathVariable Long id) {
        return categoryService.update(requestDto, id);
    }

    @Operation(
            summary = "Delete a category",
            description = """
                    Deletes a category by its ID.
                    \nNecessary role: **ADMIN**
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Category successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
            @Parameter(description = "Category ID", required = true, example = "1")
            @PathVariable Long id) {
        categoryService.delete(id);
    }

    @Operation(
            summary = "Get category by ID",
            description = """
                    Retrieves a category by its unique ID.
                    \nNecessary role: **USER**
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/{id}")
    public CategoryDto getCategoryById(
            @Parameter(description = "Category ID", required = true, example = "1")
            @PathVariable Long id) {
        return categoryService.getById(id);
    }

    @Operation(
            summary = "Get all categories",
            description = """
                    Retrieves a paginated list of
                    all categories with optional sorting.
                    \nNecessary role: **USER**
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "List of categories retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public Page<CategoryDto> getAll(
            @Parameter(description = "Pagination and sorting parameters")
            @PageableDefault(size = 5) Pageable pageable) {
        return categoryService.getAll(pageable);
    }

    @Operation(
            summary = "Get category by title",
            description = "Retrieves a category based on the given title.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryDto.class))),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    @GetMapping("/title")
    public CategoryDto getByTitle(
            @Parameter(description = "Category title", required = true, example = "Furniture")
            @RequestParam String title) {
        return categoryService.getByTitle(title);
    }

    @Operation(
            summary = "Get categories by type",
            description = """
                    Retrieves categories filtered by their
                    type with pagination support.
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Categories retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("/type")
    public Page<CategoryDto> getAllByStatus(
            @Parameter(description = "Category type",
                    required = true, example = "DESIGN")
            @RequestParam Category.CategoryType type,
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        return categoryService.getAllByType(pageable, type);
    }
}
