package com.example.PriceDetector.service;



import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Objects;


import org.springframework.stereotype.Component;

@Component
public class AiClient
{

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.model}")
    private String model;

    private final RestClient restClient = RestClient.create();

    public String analyze(String prompt)
    {

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 1024,
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

    public String analyzeWithPhoto(String prompt, String base64Image)
    {
        Map<String, Object> requestPhoto = Map.of(

                "model", model,
                "max_tokens", 1024,
                "messages", List.of(
                        Map.of("role", "user", "content", List.of(
                                Map.of("type", "text", "text", prompt),
                                Map.of("type", "image", "source", Map.of(
                                        "type", "base64",
                                        "media_type", "image/jpeg",
                                        "data", base64Image
                                ))
                        ))
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
