package org.example.nordicnestshop.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.example.nordicnestshop.dto.product.attribute.CreateAttributeDto;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class CreateProductDto {
    @Size(min = 3, max = 255)
    @NotBlank
    private String title;

    @Size(max = 255)
    @NotBlank
    private String description;

    @Size(max = 255)
    @NotBlank
    private String article;

    @DecimalMin(value = "0.01")
    private BigDecimal price;

    @Size(max = 10)
    private List<MultipartFile> images;

    @Size(max = 10)
    private List<Long> categoryIds;

    @Size(max = 50)
    private List<CreateAttributeDto> attributes;
}
