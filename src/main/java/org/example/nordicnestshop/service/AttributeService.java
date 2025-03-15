package org.example.nordicnestshop.service;

import java.util.List;
import java.util.Map;
import org.example.nordicnestshop.model.product.Product;
import org.springframework.data.domain.Page;

public interface AttributeService {
    Map<String, List<String>> getAvailableAttributes(Page<Product> products);
}
