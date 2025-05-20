package org.example.nordicnestshop.service.desing.ai.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.example.nordicnestshop.dto.design.ai.DesignAiDto;
import org.example.nordicnestshop.repository.CategoryRepository;
import org.example.nordicnestshop.service.desing.ai.DesignAiClient;
import org.example.nordicnestshop.service.desing.ai.DesignAiService;
import org.example.nordicnestshop.service.desing.ai.param.processor.DesignFilters;
import org.example.nordicnestshop.service.desing.ai.param.processor.ImageParamFilters;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class DesignAiServiceImpl implements DesignAiService {
    private final DesignAiClient designAiClient;
    private final CategoryRepository categoryRepository;
    private final List<ImageParamFilters> imageParamFilters;

    @Override
    public DesignAiDto getRecommendedCategoriesId(MultipartFile image) {
        DesignAiDto designAiDto = new DesignAiDto();
        Map<String, Map<String, Float>> parametersMap;
        Map<String, Float> filterParameters = new HashMap<>();
        try {
            parametersMap = designAiClient.getDesignParameters(image);
        } catch (IOException e) {
            throw new RuntimeException("Failed to process the image: "
                    + e.getMessage());
        }

        for (ImageParamFilters imageParamFilters : this.imageParamFilters) {
            Map<String, Float> parameters = parametersMap.get(imageParamFilters.getProcessorName());
            if (parameters != null && !parameters.isEmpty()) {
                filterParameters.putAll(imageParamFilters.filter(parameters));
                if (imageParamFilters.getClass() == DesignFilters.class) {
                    designAiDto.setDesignParameters(imageParamFilters.filter(parameters));
                }
            }
        }
        designAiDto.setSearchUrl(
                createSearchUrl(categoryRepository
                        .getCategoriesIdByTitleIn(new ArrayList<>(filterParameters.keySet())))
        );

        return designAiDto;
    }

    private String createSearchUrl(List<Long> categoryIds) {
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromPath("/products/search");

        String categoryIdsParam = categoryIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        uriBuilder.queryParam("categoryIds", categoryIdsParam);

        return uriBuilder.build().toUriString();
    }
}
