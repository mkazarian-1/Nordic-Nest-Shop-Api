package org.example.nordicnestshop.repository.specification.impl;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.example.nordicnestshop.model.Category;
import org.example.nordicnestshop.model.product.Product;
import org.example.nordicnestshop.repository.CategoryRepository;
import org.example.nordicnestshop.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class CategorySpecificationProvider implements SpecificationProvider {
    private Map<Category.CategoryType, List<Long>> categoryTypeListMap;
    private final CategoryRepository categoryRepository;

    public CategorySpecificationProvider(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public String getName() {
        return "categoryIds";
    }

    @Override
    public Specification<Product> getSpecification() {
        return (root, query, criteriaBuilder) ->
                categoryTypeListMap.values().stream()
                        .map(list -> {
                            Subquery<Long> subquery = query.subquery(Long.class);
                            Root<Product> subRoot = subquery.from(Product.class);
                            Join<Product, Category> categoryJoin = subRoot.join("categories");

                            subquery.select(subRoot.get("id"))
                                    .where(categoryJoin.get("id").in(list));

                            return root.get("id").in(subquery);
                        })
                        .reduce(criteriaBuilder::and)
                        .orElse(criteriaBuilder.conjunction());
    }

    @Override
    public void parseValues(String value) {
        List<Long> categoryIds = Arrays.stream(value
                        .split(","))
                .map(Long::parseLong)
                .toList();

        categoryTypeListMap = categoryRepository
                .getCategoriesByIdIn(categoryIds)
                .stream()
                .collect(Collectors.groupingBy(Category::getType,
                        Collectors.mapping(Category::getId, Collectors.toList())));
    }
}
