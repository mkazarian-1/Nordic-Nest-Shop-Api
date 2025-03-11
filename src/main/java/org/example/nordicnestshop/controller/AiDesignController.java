package org.example.nordicnestshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Tag(name = "AI Design Recommendations (Beta version)",
        description = "Endpoints for AI-driven product recommendations based on room images")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai-design")
public class AiDesignController {
    @Operation(summary = "Get Base Product Recommendations",
            description = """
                    Uploads an image and  AI-based product recommendations
                    without any specific category filtering.
                    \nNecessary role: None
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Recommendations retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request (e.g., missing image)",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })

    @PostMapping("/base-recommendation")
    public ResponseEntity<Map<String, String>> getBaseRecommendation(
            @Parameter(description = "Image of the room for AI analysis", required = true)
            @RequestPart("image") MultipartFile image) {

        // Process the image and extract attributes (assuming this would be implemented)
        Map<String, List<String>> extractedAttributes = new HashMap<>();

        // Build query parameters dynamically
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromPath("/products/search");

        extractedAttributes.forEach((key, values) ->
                values.forEach(value -> uriBuilder.queryParam(key, value))
        );

        String searchUrl = uriBuilder.toUriString();

        // Return the search URL in the response body instead of redirecting
        Map<String, String> response = new HashMap<>();
        response.put("searchUrl", searchUrl);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Product Recommendations by Type",
            description = """
                    Uploads an image and filters AI-generated recommendations
                    based on a specific product type category.
                    \nNecessary role: None
                    """)
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Filtered recommendations retrieved successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid request (e.g., missing parameters)",
                    content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content)
    })
    @PostMapping("/recommendation-by-type")
    public ResponseEntity<Map<String, String>> getRecommendationByProductType(
            @Parameter(description = "Image of the room for AI analysis", required = true)
            @RequestPart("image") MultipartFile image,
            @Parameter(description = "ID of the product type category",
                    required = true, example = "1")
            @RequestParam("typeCategoryId") Long typeCategoryId,
            Pageable pageable) {

        // Process the image and extract attributes (assuming this would be implemented)
        Map<String, List<String>> extractedAttributes = new HashMap<>();
        extractedAttributes.put("categoryIds", List.of(String.valueOf(typeCategoryId)));

        // Build query parameters dynamically
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromPath("/products/search");

        extractedAttributes.forEach((key, values) ->
                values.forEach(value -> uriBuilder.queryParam(key, value))
        );

        String searchUrl = uriBuilder.toUriString();

        // Return the search URL in the response body instead of redirecting
        Map<String, String> response = new HashMap<>();
        response.put("searchUrl", searchUrl);

        return ResponseEntity.ok(response);
    }
}
