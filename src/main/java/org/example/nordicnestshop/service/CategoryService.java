package org.example.nordicnestshop.service;

import org.example.nordicnestshop.dto.category.CategoryDto;
import org.example.nordicnestshop.dto.category.CreateCategoryDto;
import org.example.nordicnestshop.dto.category.UpdateCategoryDto;
import org.example.nordicnestshop.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface CategoryService {
    CategoryDto save(CreateCategoryDto categoryDto);

    CategoryDto update(UpdateCategoryDto updateCategoryDto, Long id);

    void delete(Long id);

    Page<CategoryDto> getAll(Pageable pageable);

    Page<CategoryDto> getAllByType(Pageable pageable, Category.CategoryType type);

    CategoryDto getByTitle(String title);

    CategoryDto getById(Long id);
}
