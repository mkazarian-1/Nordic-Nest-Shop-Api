package org.example.nordicnestshop.repository.specification.impl;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.example.nordicnestshop.model.product.Product;
import org.example.nordicnestshop.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class SemanticSearchSpecificationProvider implements SpecificationProvider {
    private String searchText;

    @Override
    public String getName() {
        return "searchText";
    }

    @Override
    public Specification<Product> getSpecification() {
        return (root, query, criteriaBuilder) -> {
            if (searchText == null || searchText.trim().isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            String[] textParts = searchText.toLowerCase().split("[,.\\s]+");
            List<String> wordPatterns = new ArrayList<>();
            List<Predicate> predicates = new ArrayList<>();

            wordPatterns.add("%" + searchText.toLowerCase() + "%");
            if (textParts.length > 1) {
                wordPatterns = Arrays.stream(textParts).map(s -> "%" + s + "%").toList();
            }

            for (String entity : wordPatterns) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        entity
                ));

                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("description")),
                        entity
                ));

                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("article")),
                        entity
                ));

                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.join("attributes").get("value")),
                        entity
                ));
            }
            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }

    @Override
    public void parseValues(String value) {
        searchText = value;
    }
}
