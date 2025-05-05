package org.example.nordicnestshop.repository.specification.impl;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.example.nordicnestshop.model.product.Product;
import org.example.nordicnestshop.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class MaxPriceSpecificationProvider implements SpecificationProvider {
    private BigDecimal maxPrice;

    @Override
    public String getName() {
        return "maxPrice";
    }

    @Override
    public Specification<Product> getSpecification() {
        return (root, query, criteriaBuilder) -> {
            if (maxPrice == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    @Override
    public void parseValues(String value) {
        maxPrice = BigDecimal.valueOf(Long.parseLong(value));
    }
}
