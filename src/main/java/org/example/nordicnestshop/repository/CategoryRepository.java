package org.example.nordicnestshop.repository;

import java.util.List;
import java.util.Optional;
import org.example.nordicnestshop.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    Page<Category> findAllByType(Pageable pageable, Category.CategoryType type);

    Optional<Category> findByTitle(String title);

    @Query("SELECT COUNT(c) FROM Category c WHERE c.id IN :ids")
    long countByIdIn(@Param("ids") List<Long> ids);

    @Modifying
    @Query(value = "DELETE FROM product_category WHERE category_id = :categoryId; "
            + "DELETE FROM categories WHERE id = :categoryId", nativeQuery = true)
    void deleteCategoryAndAssociations(@Param("categoryId") Long categoryId);

    List<Category> getCategoriesByIdIn(List<Long> id);

    @Query("SELECT c.id FROM Category c WHERE c.title IN :titles")
    List<Long> getCategoriesIdByTitleIn(@Param("titles") List<String> titles);
}
