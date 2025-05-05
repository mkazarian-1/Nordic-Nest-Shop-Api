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
public class MinPriceSpecificationProvider implements SpecificationProvider {
    private BigDecimal minPrice;

    @Override
    public String getName() {
        return "minPrice";
    }

    @Override
    public Specification<Product> getSpecification() {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    @Override
    public void parseValues(String value) {
        minPrice = BigDecimal.valueOf(Long.parseLong(value));
    }
}
