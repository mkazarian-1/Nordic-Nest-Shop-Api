package org.example.nordicnestshop.service.desing.ai.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.example.nordicnestshop.service.desing.ai.DesignAiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class DesignAiClientImpl implements DesignAiClient {
    private static final String DESIGN_AI_ENDPOINT = "/api/base_img_analyse/";
    private static final int TIMEOUT = 30; //seconds
    private final ObjectMapper objectMapper;
    private final OkHttpClient httpClient;

    @Value("${design.ai.api}")
    private String designAiApiUrl;

    public DesignAiClientImpl(@Autowired ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public Map<String, Map<String, Float>> getDesignParameters(MultipartFile image)
            throws IOException {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image is null or empty");
        }

        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image")) {
            contentType = determineContentType(contentType);
        }

        MultipartBody.Builder bodyBuilder = new MultipartBody
                .Builder()
                .setType(MultipartBody.FORM);

        bodyBuilder.addFormDataPart(
                "image",
                image.getOriginalFilename(),
                RequestBody.create(image.getBytes(), MediaType.parse(contentType))
        );

        Request request = new Request.Builder()
                .url(designAiApiUrl + DESIGN_AI_ENDPOINT)
                .post(bodyBuilder.build())
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body()
                        .string() : "No response from Design AI";
                throw new IOException("API request failed with status code: "
                        + response.code()
                        + ", response: " + errorBody);
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            return parseResponse(responseBody);
        }
    }

    private String determineContentType(String filename) {
        try {
            Path path = Paths.get(filename);
            return Files.probeContentType(path);
        } catch (Exception e) {
            return "application/octet-stream";
        }
    }

    private Map<String, Map<String, Float>> parseResponse(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse JSON", e);
        }
    }
}
