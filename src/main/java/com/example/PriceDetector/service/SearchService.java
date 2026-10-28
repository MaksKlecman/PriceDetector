package com.example.PriceDetector.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    @Value("${serper.api.key}")
    private String apiKey;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String searchRealPrices(String brand, String itemName)
    {
        String query = brand + " " + itemName + " buy used price and new price ";

        Map<String, Object> requestBody = Map.of("q", query);

        try {
            String response = restClient.post()
                    .uri("https://google.serper.dev/search")
                    .header("X-API-KEY", apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(response);
            JsonNode organicResults = root.get("organic");

            StringBuilder searchContext = new StringBuilder();
            if (organicResults != null && organicResults.isArray()) {
                int limit = Math.min(organicResults.size(), 5);
                for (int i = 0; i < limit; i++) {
                    JsonNode result = organicResults.get(i);
                    String title = result.has("title") ? result.get("title").asText() : "";
                    String snippet = result.has("snippet") ? result.get("snippet").asText() : "";
                    searchContext.append("- ").append(title).append(": ").append(snippet).append("\n");
                }
            }
            return searchContext.toString();
        } catch (Exception e) {
            System.err.println("Web search failed: " + e.getMessage());
            return "No real-time web search data available.";
        }
    }
}