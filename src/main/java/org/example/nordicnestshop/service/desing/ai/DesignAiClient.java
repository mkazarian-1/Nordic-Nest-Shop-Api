package org.example.nordicnestshop.service.desing.ai;

import java.io.IOException;
import java.util.Map;
import org.springframework.web.multipart.MultipartFile;

public interface DesignAiClient {
    Map<String, Map<String,Float>> getDesignParameters(MultipartFile image) throws IOException;
}
