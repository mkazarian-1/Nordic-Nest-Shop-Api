package org.example.nordicnestshop.service.desing.ai;

import org.example.nordicnestshop.dto.design.ai.DesignAiDto;
import org.springframework.web.multipart.MultipartFile;

public interface DesignAiService {
    DesignAiDto getRecommendedCategoriesId(MultipartFile image);
}
