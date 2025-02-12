package org.example.nordicnestshop.mapper;

import org.example.nordicnestshop.config.MapperConfig;
import org.example.nordicnestshop.dto.category.CategoryDto;
import org.example.nordicnestshop.dto.category.CreateCategoryDto;
import org.example.nordicnestshop.dto.category.UpdateCategoryDto;
import org.example.nordicnestshop.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.data.domain.Page;

@Mapper(config = MapperConfig.class)
public interface CategoryMapper {
    CategoryDto toDto(Category category);

    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "id", ignore = true)
    Category toEntity(CreateCategoryDto categoryDto);

    @Mapping(target = "imageUrl", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntity(UpdateCategoryDto requestCarDto, @MappingTarget Category category);
}
