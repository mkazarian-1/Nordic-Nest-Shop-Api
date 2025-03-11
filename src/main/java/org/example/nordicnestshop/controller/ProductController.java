package org.example.nordicnestshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.example.nordicnestshop.dto.product.CreateProductDto;
import org.example.nordicnestshop.dto.product.ProductFullDto;
import org.example.nordicnestshop.dto.product.ProductSearchResponseDto;
import org.example.nordicnestshop.dto.product.UpdateProductDto;
import org.example.nordicnestshop.service.ProductService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Product Management",
        description = """
                Endpoints for managing products,
                including creation, updates, deletion, and retrieval.
                """)
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @Operation(
            summary = "Create a new product",
            description = """
                    Creates a new product with
                    specified attributes and images.
                    Necessary role: **ADMIN**"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201",
                    description = "Product successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductFullDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductFullDto createProduct(
            @Parameter(description = "Product creation request data",
                    required = true)
            @Valid CreateProductDto requestDto) {
        return productService.create(requestDto);
    }

    @Operation(
            summary = "Update an existing product",
            description = """
                    Updates the product with the given ID.
                    Necessary role: **ADMIN**"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Product successfully updated",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductFullDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ProductFullDto updateProduct(
            @Parameter(description = "Updated product data", required = true)
            @Valid UpdateProductDto requestDto,
            @Parameter(description = "Product ID", required = true, example = "1")
            @PathVariable Long id) {
        return productService.update(id, requestDto);
    }

    @Operation(
            summary = "Delete a product",
            description = """
                    Deletes a product by its ID.
                    Necessary role: **ADMIN**"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204",
                    description = "Product successfully deleted"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(
            @Parameter(description = "Product ID", required = true, example = "1")
            @PathVariable Long id) {
        productService.delete(id);
    }

    @Operation(
            summary = "Get product by ID",
            description = "Retrieves a product by its unique ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductFullDto.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ProductFullDto getProductById(
            @Parameter(description = "Product ID", required = true, example = "1")
            @PathVariable Long id) {
        return productService.getById(id);
    }

    @Operation(
            summary = "Search products with advanced filtering",
            description =
                    """
                            Retrieves a paginated list of products based
                            on various search criteria,
                            including category IDs, semantic search text,
                            price range, and additional attributes.
                            **Filtering Options:**
                            
                            - Filter by category IDs (`categoryIds`)
                            - Perform a semantic search by product name or description
                             (`searchText`)
                            - Set a price range (`minPrice` and `maxPrice`)
                            - Apply attribute-based filtering (`attributes`)
                            
                            **Pagination & Sorting:**
                            
                            - Supports pageable parameters (`page`, `size`)
                            
                            **Example Usage:**
                            
                            `/products/search?categoryIds=1,2,3
                            &searchText=modern chair&minPrice=50&maxPrice=500
                            &color=red,size&large&page_number=0&page_number=10
                            """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Successfully retrieved products"),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/search")
    public ProductSearchResponseDto getAllByCategoryIdsAndAttributes(
            @Parameter(
                    description = "Additional filtering attributes in key-value format",
                    example = "color=red,green"
            )
            @RequestParam(required = false) Map<String, String> attributes,

            @Parameter(
                    description = """
                            Pagination and sorting parameters
                            (`page_number`, `page_size`)
                            """,
                    example = "page_number=0&page_size=10"
            )
            Pageable pageable) {
        return productService.getAllByCategoryIdsAndAttributes(attributes, pageable);
    }
}
