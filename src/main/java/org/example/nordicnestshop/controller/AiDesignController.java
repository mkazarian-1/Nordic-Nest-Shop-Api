package org.example.nordicnestshop.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.nordicnestshop.dto.design.ai.DesignAiDto;
import org.example.nordicnestshop.service.desing.ai.DesignAiService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "AI Design Recommendations (Beta version)",
        description = "Endpoints for AI-driven product recommendations based on room images")
@RestController
@RequiredArgsConstructor
@RequestMapping("/ai-design")
public class AiDesignController {

    private final DesignAiService designAiService;

    @Operation(
            summary = "Get Base Product Recommendations",
            description = """
                    Upload a room image to receive product recommendations using AI analysis.
                    This endpoint uses visual features (e.g., room type, style) to infer categories.
                    No authentication required.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "AI-based recommendations retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = DesignAiDto.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Bad request, e.g. missing or invalid image file",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Server error during image processing or AI prediction",
                    content = @Content
            )
    })
    @PostMapping(value = "/base-recommendation", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public DesignAiDto getBaseRecommendation(
            @Parameter(description = "Room image file for AI classification", required = true)
            @RequestPart("image") MultipartFile image) {

        return designAiService.getRecommendedCategoriesId(image);
    }
}
