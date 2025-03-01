package org.example.nordicnestshop.repository;

import java.util.List;
import org.example.nordicnestshop.model.product.Product;
import org.example.nordicnestshop.model.product.ProductImage;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
    @EntityGraph(attributePaths = "product")
    List<ProductImage> findAllByProductIn(List<Product> products);
}
