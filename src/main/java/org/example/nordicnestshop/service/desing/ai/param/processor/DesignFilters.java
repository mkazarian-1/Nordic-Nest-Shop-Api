package org.example.nordicnestshop.service.desing.ai.param.processor;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class DesignFilters implements ImageParamFilters {
    @Override
    public String getProcessorName() {
        return "design_classifier";
    }

    @Override
    public Map<String, Float> filter(Map<String, Float> parameters) {
        float[] sum = {0}; // Using array wrapper to mutate within lambda

        return parameters.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .takeWhile(entry -> {
                    if (sum[0] >= 0.8f) {
                        return false;
                    }
                    sum[0] += entry.getValue();
                    return true;
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
