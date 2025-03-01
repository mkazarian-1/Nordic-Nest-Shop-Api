package org.example.nordicnestshop.service.impl;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.nordicnestshop.model.product.Attribute;
import org.example.nordicnestshop.model.product.Product;
import org.example.nordicnestshop.repository.AttributeRepository;
import org.example.nordicnestshop.service.AttributeService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttributeServiceImpl implements AttributeService {
    @Override
    public Map<String, List<String>> getAvailableAttributes(Page<Product> products) {
        return products.stream()
                .flatMap(product -> product.getAttributes().stream())
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(
                        Attribute::getKey,
                        Collectors.mapping(
                                Attribute::getValue,
                                Collectors.collectingAndThen(
                                        Collectors.toSet(),
                                        set -> set.stream().sorted().collect(Collectors.toList())
                                )
                        )
                ));
    }
}
