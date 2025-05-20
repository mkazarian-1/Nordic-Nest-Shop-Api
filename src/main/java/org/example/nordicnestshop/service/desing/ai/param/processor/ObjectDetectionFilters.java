package org.example.nordicnestshop.service.desing.ai.param.processor;

import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ObjectDetectionFilters implements ImageParamFilters {
    @Override
    public String getProcessorName() {
        return "furniture_detector";
    }

    @Override
    public Map<String, Float> filter(Map<String, Float> parameters) {
        return parameters;
    }
}
