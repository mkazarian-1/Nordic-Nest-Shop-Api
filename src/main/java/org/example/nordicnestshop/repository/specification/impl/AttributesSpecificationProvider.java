package org.example.nordicnestshop.repository.specification.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.NotImplementedException;
import org.example.nordicnestshop.model.product.Attribute;
import org.example.nordicnestshop.model.product.Product;
import org.example.nordicnestshop.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class AttributesSpecificationProvider implements SpecificationProvider {
    private Map<String, List<String>> attributes;

    @Override
    public String getName() {
        return "attributes";
    }

    @Override
    public Specification<Product> getSpecification() {
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
    public void parseValues(String value) {
        throw new NotImplementedException("Method parseValues() is not implemented.");
    }

    private Predicate buildAttributePredicates(CriteriaBuilder criteriaBuilder,
                                               Root<Attribute> subRoot,
                                               Map<String, List<String>> attributes) {
        return attributes.entrySet().stream()
                .map(entry -> criteriaBuilder.and(
                        criteriaBuilder.equal(subRoot.get("key"), entry.getKey()),
                        subRoot.get("value").in(entry.getValue())
                ))
                .reduce(criteriaBuilder::or)
                .orElse(criteriaBuilder.conjunction());
    }
}
