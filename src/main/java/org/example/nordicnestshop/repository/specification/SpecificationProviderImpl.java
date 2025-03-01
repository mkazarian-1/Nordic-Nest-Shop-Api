package org.example.nordicnestshop.repository.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.example.nordicnestshop.model.Category;
import org.example.nordicnestshop.model.product.Attribute;
import org.example.nordicnestshop.model.product.Product;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class SpecificationProviderImpl implements SpecificationProvider {
    public Specification<Product> hasCategoryIds(List<Long> categoryIds) {
        return (root, query, criteriaBuilder) -> {
            if (categoryIds == null || categoryIds.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            Subquery<Long> subquery = (query != null) ? query.subquery(Long.class) : null;
            if (subquery == null) {
                return criteriaBuilder.conjunction();
            }

            Root<Product> subRoot = subquery.from(Product.class);
            Join<Product, Category> categoryJoin = subRoot.join("categories");

            subquery.select(subRoot.get("id"))
                    .where(categoryJoin.get("id").in(categoryIds))
                    .groupBy(subRoot.get("id"))
                    .having(criteriaBuilder.equal(
                            criteriaBuilder.count(categoryJoin.get("id")),
                            categoryIds.size()
                    ));

            return criteriaBuilder.in(root.get("id")).value(subquery);
        };
    }

    public Specification<Product> hasAttributes(Map<String, List<String>> attributes) {
        return (root, query, criteriaBuilder) -> {
            if (attributes == null || attributes.isEmpty()) {
                return criteriaBuilder.conjunction();
            }

            Subquery<Long> subquery = (query != null) ? query.subquery(Long.class) : null;
            if (subquery == null) {
                return criteriaBuilder.conjunction();
            }
            Root<Attribute> subRoot = subquery.from(Attribute.class);

            subquery.select(subRoot.get("product").get("id"))
                    .where(buildAttributePredicates(criteriaBuilder, subRoot, attributes))
                    .groupBy(subRoot.get("product").get("id"))
                    .having(criteriaBuilder.equal(criteriaBuilder
                            .countDistinct(subRoot.get("key")), attributes.size()));

            return criteriaBuilder.in(root.get("id")).value(subquery);
        };
    }

    @Override
    public Specification<Product> semanticSearch(String searchText) {
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

    private Predicate buildAttributePredicates(CriteriaBuilder criteriaBuilder,
                                               Root<Attribute> subRoot,
                                               Map<String, List<String>> attributes) {
        List<Predicate> predicates = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : attributes.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();

            Predicate keyPredicate = criteriaBuilder.equal(subRoot.get("key"), key);
            Predicate valuePredicate = subRoot.get("value").in(values);

            predicates.add(criteriaBuilder.and(keyPredicate, valuePredicate));
        }

        return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
    }
}
