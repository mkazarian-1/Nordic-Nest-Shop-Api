package org.example.nordicnestshop.repository;

import org.example.nordicnestshop.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    Page<Category> findAllByType(Pageable pageable, Category.CategoryType Type);

    Optional<Category> findByTitle(String title);
}
