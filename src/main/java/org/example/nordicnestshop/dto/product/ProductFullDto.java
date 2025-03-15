package org.example.nordicnestshop.dto.product;

import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.example.nordicnestshop.dto.product.attribute.AttributeDto;

@Getter
@Setter
public class ProductFullDto {
    private Long id;

    private String title;

    private String description;

    private String article;

    private BigDecimal price;

    private List<String> images;

    private List<Long> categoryIds;

    private List<AttributeDto> attributes;
}
