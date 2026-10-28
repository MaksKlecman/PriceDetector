package com.example.PriceDetector.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Component
public class AiClient {

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.model}")
    private String model;

    private final RestClient restClient = RestClient.create();

    public String analyze(String prompt)
    {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 2048,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        return restClient.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .body(requestBody)
                .retrieve()
                .body(String.class);
    }

    public String analyzeWithPhoto(String prompt, List<MultipartFile> files) throws IOException
    {
        List<Map<String, Object>> contentList = new ArrayList<>();
        contentList.add(Map.of("type", "text", "text", prompt));

        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;

            String base64Data = Base64.getEncoder().encodeToString(file.getBytes());

            // Динамічно визначаємо MIME-тип файлу (jpeg, png, webp, gif)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = "image/jpeg"; // fallback
            }

            contentList.add(Map.of(
                    "type", "image",
                    "source", Map.of(
                            "type", "base64",
                            "media_type", contentType,
                            "data", base64Data
                    )
            ));


        }

        Map<String, Object> requestPhoto = Map.of(
                "model", model,
                "max_tokens", 2048,
                "messages", List.of(
                        Map.of("role", "user", "content", contentList)
                )
        );





        return restClient.post()
                .uri("https://api.anthropic.com/v1/messages")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .header("content-type", "application/json")
                .body(requestPhoto)
                .retrieve()
                .body(String.class);



    }
}