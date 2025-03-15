package org.example.nordicnestshop.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.nordicnestshop.dto.category.CategoryDto;
import org.example.nordicnestshop.dto.category.CreateCategoryDto;
import org.example.nordicnestshop.dto.category.UpdateCategoryDto;
import org.example.nordicnestshop.exception.ElementNotFoundException;
import org.example.nordicnestshop.mapper.CategoryMapper;
import org.example.nordicnestshop.model.Category;
import org.example.nordicnestshop.repository.CategoryRepository;
import org.example.nordicnestshop.service.CategoryService;
import org.example.nordicnestshop.service.amazon.s3.S3Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final S3Service s3Service;

    @Override
    @Transactional
    public CategoryDto create(CreateCategoryDto categoryDto) {
        Category newCategory = categoryMapper.toEntity(categoryDto);
        newCategory.setImageUrl(s3Service.uploadFile(categoryDto.getImage()));
        return categoryMapper.toDto(categoryRepository.save(newCategory));
    }

    @Override
    @Transactional
    public CategoryDto update(UpdateCategoryDto updateCategoryDto, Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new ElementNotFoundException("Can't find Category object by ID:" + id));

        categoryMapper.updateEntity(updateCategoryDto, category);

        if (updateCategoryDto.getImage() != null && !updateCategoryDto.getImage().isEmpty()) {
            String newUrl = s3Service.uploadFile(updateCategoryDto.getImage());
            s3Service.deleteFile(category.getImageUrl());
            category.setImageUrl(newUrl);
        }

        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(
                () -> new ElementNotFoundException("Can't find Category object by ID:" + id));

        s3Service.deleteFile(category.getImageUrl());
        categoryRepository.deleteCategoryAndAssociations(category.getId());
    }

    @Override
    public Page<CategoryDto> getAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(categoryMapper::toDto);
    }

    @Override
    public Page<CategoryDto> getAllByType(Pageable pageable, Category.CategoryType type) {
        return categoryRepository.findAllByType(pageable, type).map(categoryMapper::toDto);
    }

    @Override
    public CategoryDto getByTitle(String title) {
        return categoryMapper.toDto(categoryRepository.findByTitle(title).orElseThrow(
                () -> new ElementNotFoundException("Can't find Category object by Title: "
                        + title)));
    }

    @Override
    public CategoryDto getById(Long id) {
        return categoryMapper.toDto(categoryRepository.findById(id).orElseThrow(
                () -> new ElementNotFoundException("Can't find Category object by ID:" + id)));
    }
}
