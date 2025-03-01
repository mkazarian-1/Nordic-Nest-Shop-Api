package org.example.nordicnestshop.service;

import java.util.List;
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

    ProductSearchResponseDto getAllByCategoryIdsAndAttributes(List<Long> categoryIds,
                                                              Map<String, String> attributes,
                                                              String searchText,
                                                              Pageable pageable);

    void delete(Long id);
}
