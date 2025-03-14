package org.example.nordicnestshop.dto.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Page;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProductSearchResponseDto {
    private Page<ProductDto> products;
    private Map<String, List<String>> availableAttributes;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
