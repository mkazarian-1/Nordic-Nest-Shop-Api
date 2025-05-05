package org.example.nordicnestshop.repository.specification;

import org.example.nordicnestshop.model.product.Product;
import org.springframework.data.jpa.domain.Specification;

public interface SpecificationProvider {
    String getName();

    Specification<Product> getSpecification();

    void parseValues(String value);
}
