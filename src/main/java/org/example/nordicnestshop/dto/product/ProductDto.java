package org.example.nordicnestshop.dto.product;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductDto {
    private Long id;

    private String title;

    private String article;

    private BigDecimal price;

    private String mainImage;
}
