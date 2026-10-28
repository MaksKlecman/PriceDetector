package com.example.PriceDetector.service;

import com.example.PriceDetector.dto.AnalysisMapper;
import com.example.PriceDetector.dto.AnalysisRequest;
import com.example.PriceDetector.dto.AnalysisResponse;
import com.example.PriceDetector.exception.AnalysisNotFoundException;
import com.example.PriceDetector.model.Analysis;
import com.example.PriceDetector.model.AuthenticResult;
import com.example.PriceDetector.model.Verdict;
import com.example.PriceDetector.repository.AnalysisRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final AnalysisRepository repository;
    private final AnalysisMapper mapper;
    private final AiClient aiClient;
    private final SearchService searchService;

    @Transactional
    public AnalysisResponse save(AnalysisRequest request)
    {
      Analysis analysis = mapper.toEntity(request);
      return mapper.toResponse(repository.save(analysis));

    }

    public AnalysisResponse getById(Long id)
    {
        return mapper.toResponse(getAnalysis(id));
    }
    @Transactional
    public List<AnalysisResponse> getAll()
    {
        return mapper.toResponseList(repository.findAll());
    }

    private Analysis getAnalysis(Long id)
    {
        return repository.findById(id)
            .orElseThrow(() -> new AnalysisNotFoundException(id));
    }



    private String buildPrompt(Analysis analysis, String marketData)
    {
        String notesLine = "";
        if (analysis.getAdditionalNotes() != null && !analysis.getAdditionalNotes().isEmpty())
        {
            notesLine = "Additional notes from user: " + analysis.getAdditionalNotes() + "\n";
        }


        return "Analyze this item for resale value:\n" +
                "Brand: " + analysis.getItemBrand() + "\n" +
                "Item name: " + analysis.getItemName() + "\n" +
                "Category: " + analysis.getCategory() + "\n" +
                "Condition: " + analysis.getCondition() + "\n" +
                "Description: " + analysis.getDescription() + "\n" +
                "Seller price: " + analysis.getSellerPrice() + "\n" +
                notesLine +
                        "\nREAL-TIME MARKET SEARCH RESULTS FROM GOOGLE/STORES:\n" +
                marketData + "\n" +
                "Use the above real-time market search results to guide your price estimations!\n" +
                "Respond ONLY with valid JSON in this exact format:\n" +
                "Respond ONLY with valid JSON in this exact format:\n" +
                "{\n" +
                "  \"estimatedNewPrice\": <number>,\n" +
                "  \"estimatedResalePrice\": <number>,\n" +
                "  \"verdict\": \"BUY\" or \"DONT_BUY\" or \"NEGOTIATE\",\n" +
                "  \"suggestedPrice\": <number>\n" +

                "}";
    }

    private String buildPromptWithPhoto(Analysis analysis, String marketData)
    {
        String notesLine = "";
        if (analysis.getAdditionalNotes() != null && !analysis.getAdditionalNotes().isEmpty())
        {
            notesLine = "Additional notes from user: " + analysis.getAdditionalNotes() + "\n";
        }

        return "Analyze this item for resale value:\n" +
                "Brand: " + analysis.getItemBrand() + "\n" +
                "Item name: " + analysis.getItemName() + "\n" +
                "Category: " + analysis.getCategory() + "\n" +
                "Condition: " + analysis.getCondition() + "\n" +
                "Description: " + analysis.getDescription() + "\n" +
                "Seller price: " + analysis.getSellerPrice() + "\n" +
                notesLine +
                        "\nREAL-TIME MARKET SEARCH RESULTS FROM GOOGLE/STORES:\n" +
                marketData + "\n" +
                "Use the above real-time market search results to guide your price estimations!\n" +
                "Respond ONLY with valid JSON in this exact format:\n" +

                "Respond ONLY with valid JSON in this exact format:\n" +
                "{\n" +
                "  \"estimatedNewPrice\": <number>,\n" +
                "  \"estimatedResalePrice\": <number>,\n" +
                "  \"verdict\": \"BUY\" or \"DONT_BUY\" or \"NEGOTIATE\",\n" +
                "  \"suggestedPrice\": <number>,\n" +
                "  \"authenticResult\": \"FAKE\" or \"NOT_ENOUGH_INFORMATION\" or \"ORIGINAL\" " +

                "}";
    }

    @Transactional
    public AnalysisResponse runAiAnalysis(Long id) throws Exception {
        Analysis analysis = getAnalysis(id);

        String marketData = searchService.searchRealPrices(analysis.getItemBrand(), analysis.getItemName());

        String initialPrompt = buildPrompt(analysis,marketData );

        String initialPromptResponse = aiClient.analyze(initialPrompt);

        JsonNode initialPromptResult = extractJsonFromAiResponse(initialPromptResponse);

        String verificationPrompt1 = buildVerificationPrompt(analysis, initialPromptResult);

        String verificationPromptResponse1 = aiClient.analyze(verificationPrompt1);

        JsonNode  verificationPromptResult1 = extractJsonFromAiResponse(verificationPromptResponse1);

        String verificationPrompt2 =
                buildVerificationPrompt2(analysis, verificationPromptResult1);

        String verificationResponse2 =
                aiClient.analyze(verificationPrompt2);

        JsonNode finalResult =
                extractJsonFromAiResponse(verificationResponse2);











        analysis.setAiRawResponse(finalResult.toString());


        applyAiResults(analysis, finalResult);

        Analysis saved = repository.save(analysis);

        return mapper.toResponse(saved);
    }

    public AnalysisResponse runAiAnalysisWithPhoto(Long id, List<MultipartFile> files) throws Exception
    {
        Analysis analysis = getAnalysis(id);

        List<String> base64Images = new ArrayList<>();

        for (int i = 0; i < files.size(); i++)
        {
            String rep = Base64.getEncoder().encodeToString(files.get(i).getBytes());
            base64Images.add(rep);
        }

        String marketData = searchService.searchRealPrices(analysis.getItemBrand(), analysis.getItemName());



        String prompt = buildPromptWithPhoto(analysis, marketData);

        String aiResponsePhoto = aiClient.analyzeWithPhoto(prompt, files);

        analysis.setAiRawResponse(aiResponsePhoto);

        JsonNode clearedResPhoto = extractJsonFromAiResponse(aiResponsePhoto);

        applyAiResultsWithPhoto( analysis, clearedResPhoto);

        Analysis saved = repository.save(analysis);

        return mapper.toResponse(saved);





    }

    private JsonNode extractJsonFromAiResponse(String aiResponse) throws Exception {

        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode root = objectMapper.readTree(aiResponse);

        JsonNode content = root.get("content");

        if (content == null || !content.isArray() || content.isEmpty()) {
            throw new IllegalArgumentException(
                    "Anthropic response does not contain valid content"
            );
        }

        JsonNode textNode = content.get(0).get("text");

        if (textNode == null || textNode.isNull()) {
            throw new IllegalArgumentException(
                    "Anthropic response does not contain text"
            );
        }

        String text = textNode.asText().trim();

        if (text.startsWith("```json")) {
            text = text.substring("```json".length());
        }

        if (text.startsWith("```")) {
            text = text.substring("```".length());
        }

        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - "```".length());
        }

        text = text.trim();

        return objectMapper.readTree(text);
    }


    private String buildVerificationPrompt(
            Analysis analysis,
            JsonNode result

    ) {

        return """
            You are a verification AI.

            Review the previous AI analysis of this item.

            ITEM:
            Brand: %s
            Name: %s
            Condition: %s
            Seller's asking price: %s

            PREVIOUS AI RESULT:
            %s

            Check the following:

            1. Is estimatedNewPrice realistic?
            2. Is estimatedResalePrice realistic compared to estimatedNewPrice?
            3. Is suggestedPrice lower than or equal to the seller's asking price?
            4. Is suggestedPrice reasonable compared to estimatedResalePrice?
            5. Does the verdict match the numbers?
            6. Are there obvious contradictions?
            7. Are any prices suspiciously identical or arbitrary?

            If something is wrong, correct it.

            If everything is correct, keep the values unchanged.

            Respond ONLY with valid JSON:

            {
              "estimatedNewPrice": <number>,
              "estimatedResalePrice": <number>,
              "verdict": "BUY" or "DONT_BUY" or "NEGOTIATE",
              "suggestedPrice": <number>
            }
            """.formatted(
                analysis.getItemBrand(),
                analysis.getItemName(),
                analysis.getCondition(),
                analysis.getSellerPrice(),
                result.toString()
        );
    }

    private String buildVerificationPrompt2(
            Analysis analysis,
            JsonNode result
    ) {

        return """
            You are the final quality-control AI.

            Your job is to independently audit the previous AI result.

            ITEM:
            Brand: %s
            Name: %s
            Condition: %s
            Seller's asking price: %s

            RESULT FROM PREVIOUS VERIFICATION:
            %s

            Perform a strict final validation.

            RULES:

            1. estimatedNewPrice must be greater than 0.

            2. estimatedResalePrice must be greater than 0.

            3. For a used item, estimatedResalePrice should normally
               be lower than estimatedNewPrice.

            4. suggestedPrice must be greater than 0.

            5. suggestedPrice should normally not exceed the seller's
               asking price.

            6. If the seller's price is significantly below the expected
               resale value, BUY is generally appropriate.

            7. If the seller's price is close to the expected resale value,
               NEGOTIATE may be appropriate.

            8. If the seller's price is significantly above the expected
               resale value, DONT_BUY is generally appropriate.

            9. Make sure the verdict is logically consistent with all prices.

            10. Look for suspiciously round, identical or contradictory
                numbers.

            IMPORTANT:
            Do not change correct values unnecessarily.
            Only change a value if there is a logical reason to do so.

            Return ONLY valid JSON:

            {
              "estimatedNewPrice": <number>,
              "estimatedResalePrice": <number>,
              "verdict": "BUY" or "DONT_BUY" or "NEGOTIATE",
              "suggestedPrice": <number>
            }
            """.formatted(
                analysis.getItemBrand(),
                analysis.getItemName(),
                analysis.getCondition(),
                analysis.getSellerPrice(),
                result.toString()
        );
    }

    private void applyAiResults(
            Analysis analysis,
            JsonNode jsonNode
    ) {

        BigDecimal newPrice =
                jsonNode.get("estimatedNewPrice").decimalValue();

        BigDecimal resalePrice =
                jsonNode.get("estimatedResalePrice").decimalValue();

        BigDecimal suggestedPrice =
                jsonNode.get("suggestedPrice").decimalValue();

        String verdict =
                jsonNode.get("verdict").asText();

        analysis.setEstimatedNewPrice(newPrice);
        analysis.setEstimatedResalePrice(resalePrice);
        analysis.setSuggestedPrice(suggestedPrice);
        analysis.setVerdict(Verdict.valueOf(verdict));
    }


    private void applyAiResultsWithPhoto(Analysis analysis, JsonNode jsonNode)
    {
        applyAiResults(analysis,jsonNode);


        String newAuthenticResult = jsonNode.get("authenticResult").asText();
        analysis.setAuthenticResult(AuthenticResult.valueOf(newAuthenticResult));



    }




}
