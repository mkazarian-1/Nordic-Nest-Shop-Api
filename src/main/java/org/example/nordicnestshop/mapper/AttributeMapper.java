package org.example.nordicnestshop.mapper;

import org.example.nordicnestshop.config.MapperConfig;
import org.example.nordicnestshop.dto.product.attribute.AttributeDto;
import org.example.nordicnestshop.dto.product.attribute.CreateAttributeDto;
import org.example.nordicnestshop.dto.product.attribute.UpdateAttributeDto;
import org.example.nordicnestshop.model.product.Attribute;
import org.mapstruct.Mapper;

@Mapper(config = MapperConfig.class)
public interface AttributeMapper {
    AttributeDto toDto(Attribute attribute);

    Attribute toEntity(CreateAttributeDto attributeDto);

    Attribute toEntity(UpdateAttributeDto attributeDto);
}
