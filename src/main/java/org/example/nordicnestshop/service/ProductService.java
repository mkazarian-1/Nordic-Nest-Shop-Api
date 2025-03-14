package org.example.nordicnestshop.service;

import java.util.Map;
import org.example.nordicnestshop.dto.product.CreateProductDto;
import org.example.nordicnestshop.dto.product.ProductFullDto;
import org.example.nordicnestshop.dto.product.ProductSearchResponseDto;
import org.example.nordicnestshop.dto.product.UpdateProductDto;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductFullDto create(CreateProductDto createProductDto);

    ProductFullDto update(Long id, UpdateProductDto updateProductDto);

    ProductFullDto getById(Long id);

    ProductSearchResponseDto getAllByCategoryIdsAndAttributes(Map<String, String> attributes,
                                                              Pageable pageable);

    void delete(Long id);
}
