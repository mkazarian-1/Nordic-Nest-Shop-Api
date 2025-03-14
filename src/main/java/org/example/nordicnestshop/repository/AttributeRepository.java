package org.example.nordicnestshop.repository;

import java.util.List;
import org.example.nordicnestshop.model.product.Attribute;
import org.example.nordicnestshop.model.product.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<Attribute, Long> {
    @EntityGraph(attributePaths = "product")
    List<Attribute> findAllByProductIn(List<Product> products);
}
