package org.example.nordicnestshop.dto.product.attribute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateAttributeDto {
    @Size(min = 1, max = 255)
    @NotBlank
    private String key;

    @Size(min = 1, max = 255)
    @NotBlank
    private String value;
}
