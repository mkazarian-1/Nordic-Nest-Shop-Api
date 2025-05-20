package org.example.nordicnestshop.dto.design.ai;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DesignAiDto {
    private String searchUrl;
    private Map<String, Float> designParameters;
}
