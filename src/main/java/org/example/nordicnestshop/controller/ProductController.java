package org.example.nordicnestshop.controller;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.nordicnestshop.dto.product.CreateProductDto;
import org.example.nordicnestshop.dto.product.ProductFullDto;
import org.example.nordicnestshop.dto.product.ProductSearchResponseDto;
import org.example.nordicnestshop.dto.product.UpdateProductDto;
import org.example.nordicnestshop.service.ProductService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping()
    //    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductFullDto createCategory(@Valid CreateProductDto requestDto) {
        return productService.create(requestDto);
    }

    @PutMapping("/{id}")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    public ProductFullDto updateCategory(@Valid UpdateProductDto requestDto,
                                         @PathVariable Long id) {
        return productService.update(id, requestDto);
    }

    @DeleteMapping("/{id}")
    //    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        productService.delete(id);
    }

    //
    @GetMapping("/{id}")
    //    @PreAuthorize("hasAuthority('USER')")
    public ProductFullDto getCategoryById(@PathVariable Long id) {
        return productService.getById(id);
    }
    //

    @GetMapping("/search")
    //    @PreAuthorize("hasAuthority('USER')")
    public ProductSearchResponseDto getAllByCategoryIdsAndAttributes(@RequestParam(required = false)
                                                                     List<Long> categoryIds,
                                                                     @RequestParam(required = false)
                                                                     Map<String, String> attributes,
                                                                     @RequestParam(required = false)
                                                                     String searchText,
                                                                     Pageable pageable) {
        if (categoryIds != null) {
            attributes.remove("categoryIds");
        }
        if (searchText != null) {
            attributes.remove("searchText");
        }
        return productService.getAllByCategoryIdsAndAttributes(categoryIds,
                attributes,
                searchText,
                pageable);
    }
}
