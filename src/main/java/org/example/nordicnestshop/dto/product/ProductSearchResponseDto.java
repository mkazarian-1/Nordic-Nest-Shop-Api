package org.example.nordicnestshop.dto.product;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@AllArgsConstructor
public class ProductSearchResponseDto {
    private Page<ProductDto> products;
    private Map<String, List<String>> availableAttributes;
}
