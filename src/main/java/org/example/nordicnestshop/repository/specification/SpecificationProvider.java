package org.example.nordicnestshop.repository.specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.example.nordicnestshop.model.product.Product;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationProvider {
    Specification<Product> hasCategoryIds(List<Long> categoryIds);

    Specification<Product> hasAttributes(Map<String, List<String>> attributes);

    Specification<Product> semanticSearch(String searchText);

    Specification<Product> minPrice(BigDecimal min);

    Specification<Product> maxPrice(BigDecimal max);
}
