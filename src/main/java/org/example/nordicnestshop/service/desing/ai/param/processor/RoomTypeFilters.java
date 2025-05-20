package org.example.nordicnestshop.service.desing.ai.param.processor;

import java.util.Collections;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RoomTypeFilters implements ImageParamFilters {
    @Override
    public String getProcessorName() {
        return "room_classifier";
    }

    @Override
    public Map<String, Float> filter(Map<String, Float> parameters) {
        return parameters.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> Map.of(entry.getKey(), entry.getValue()))
                .orElseGet(Collections::emptyMap);
    }
}
