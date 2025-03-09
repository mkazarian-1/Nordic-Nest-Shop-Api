package org.example.nordicnestshop.mapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.example.nordicnestshop.config.MapperConfig;
import org.example.nordicnestshop.dto.product.CreateProductDto;
import org.example.nordicnestshop.dto.product.ProductDto;
import org.example.nordicnestshop.dto.product.ProductFullDto;
import org.example.nordicnestshop.dto.product.UpdateProductDto;
import org.example.nordicnestshop.dto.product.attribute.CreateAttributeDto;
import org.example.nordicnestshop.dto.product.attribute.UpdateAttributeDto;
import org.example.nordicnestshop.model.Category;
import org.example.nordicnestshop.model.product.Attribute;
import org.example.nordicnestshop.model.product.Product;
import org.example.nordicnestshop.model.product.ProductImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class, uses = {AttributeMapper.class})
public interface ProductMapper {

    @Mapping(target = "categoryIds", source = "categories",
            qualifiedByName = "setCategoriesToIds")
    @Mapping(target = "images", source = "images", qualifiedByName = "setImagesUrl")
    ProductFullDto toFullDto(Product product);

    @Mapping(target = "mainImage", source = "images", qualifiedByName = "setMainImagesUrl")
    ProductDto toDto(Product product);

    @Mapping(target = "categories", source = "categoryIds",
            qualifiedByName = "setCategoriesFromIds")
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "attributes",
            expression = "java(this.setAttributesWithProduct"
                    + "(productDto.getAttributes(), product, attributeMapper))"
    )
    Product toEntity(CreateProductDto productDto);

    @Mapping(target = "images", ignore = true)
    @Mapping(target = "attributes",
            expression = "java(this.updateAttributesWithProduct"
                    + "(productDto.getAttributes(), product, attributeMapper))"
    )
    @Mapping(target = "categories", source = "categoryIds",
            qualifiedByName = "setCategoriesFromIds")
    void updateEntity(UpdateProductDto productDto, @MappingTarget Product product);

    @Named("setCategoriesFromIds")
    default Set<Category> setCategoriesFromIds(List<Long> categoryIds) {
        if (categoryIds == null) {
            return new HashSet<>();
        }
        return categoryIds.stream()
                .map(id -> {
                    Category category = new Category();
                    category.setId(id);
                    return category;
                })
                .collect(Collectors.toSet());
    }

    @Named("setCategoriesToIds")
    default List<Long> setCategoriesToIds(Set<Category> categories) {
        if (categories == null) {
            return new ArrayList<>();
        }
        return categories.stream()
                .map(Category::getId)
                .collect(Collectors.toList());
    }

    @Named("setImagesUrl")
    default List<String> setImagesUrl(List<ProductImage> images) {
        if (images == null) {
            return new ArrayList<>();
        }
        return images.stream()
                .map(ProductImage::getImageUrl)
                .collect(Collectors.toList());
    }

    @Named("setMainImagesUrl")
    default String setMainImagesUrl(List<ProductImage> images) {
        if (images == null) {
            return "";
        }
        return images.stream().filter(img -> img.getOrderIndex() == 0)
                .findFirst()
                .orElse(new ProductImage())
                .getImageUrl();
    }

    default Set<Attribute> setAttributesWithProduct(List<CreateAttributeDto> attributes,
                                                    @MappingTarget Product product,
                                                    AttributeMapper attributeMapper) {
        if (attributes == null) {
            return new HashSet<>();
        }
        return attributes.stream().map(a -> {
            Attribute attribute = attributeMapper.toEntity(a);
            attribute.setProduct(product);
            return attribute;
        }).collect(Collectors.toSet());
    }

    default Set<Attribute> updateAttributesWithProduct(List<UpdateAttributeDto> attributes,
                                                       @MappingTarget Product product,
                                                       AttributeMapper attributeMapper) {
        Set<Attribute> newSet;
        if (attributes == null) {
            newSet = new HashSet<>();
        } else {
            newSet = attributes.stream().map(a -> {
                Attribute attribute = attributeMapper.toEntity(a);
                attribute.setProduct(product);
                return attribute;
            }).collect(Collectors.toSet());
        }
        product.getAttributes().clear();
        product.getAttributes().addAll(newSet);
        return product.getAttributes();
    }
}
