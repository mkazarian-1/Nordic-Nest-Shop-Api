package org.example.nordicnestshop.service.desing.ai.param.processor;

import java.util.Map;

public interface ImageParamFilters {
    String getProcessorName();

    Map<String,Float> filter(Map<String,Float> parameters);
}
