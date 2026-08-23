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



    private String buildPrompt(Analysis analysis)
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
                "Respond ONLY with valid JSON in this exact format:\n" +
                "{\n" +
                "  \"estimatedNewPrice\": <number>,\n" +
                "  \"estimatedResalePrice\": <number>,\n" +
                "  \"verdict\": \"BUY\" or \"DONT_BUY\" or \"NEGOTIATE\",\n" +
                "  \"suggestedPrice\": <number>\n" +

                "}";
    }

    private String buildPromptWithPhoto(Analysis analysis)
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

                "Respond ONLY with valid JSON in this exact format:\n" +
                "{\n" +
                "  \"estimatedNewPrice\": <number>,\n" +
                "  \"estimatedResalePrice\": <number>,\n" +
                "  \"verdict\": \"BUY\" or \"DONT_BUY\" or \"NEGOTIATE\",\n" +
                "  \"suggestedPrice\": <number>,\n" +
                "  \"authenticResult\": \"FAKE\" or \" NOT_ENOUGH_INFORMATION\" or \"ORIGINAL\" " +

                "}";
    }

    @Transactional
    public AnalysisResponse runAiAnalysis(Long id) throws Exception {
        Analysis analysis = getAnalysis(id);

        String prompt = buildPrompt(analysis);

        String aiResponse = aiClient.analyze(prompt);


        analysis.setAiRawResponse(aiResponse);

        JsonNode clearedRes = extractJsonFromAiResponse(aiResponse);

        applyAiResults(analysis, clearedRes);

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



        String prompt = buildPromptWithPhoto(analysis);

        String aiResponsePhoto = aiClient.analyzeWithPhoto(prompt, base64Images);

        analysis.setAiRawResponse(aiResponsePhoto);

        JsonNode clearedResPhoto = extractJsonFromAiResponse(aiResponsePhoto);

        applyAiResultsWithPhoto( analysis, clearedResPhoto);

        Analysis saved = repository.save(analysis);

        return mapper.toResponse(saved);





    }

    private JsonNode extractJsonFromAiResponse(String aiRawResponse) throws Exception
    {
        ObjectMapper mapper1 = new ObjectMapper();

        JsonNode root = mapper1.readTree(aiRawResponse);

        JsonNode contentArray = root.get("content");

        JsonNode firstItem = contentArray.get(0);

        String text = firstItem.get("text").asText();

        String cleanedText = text.replace("```json", "").replace("```", "").trim();



        return mapper1.readTree(cleanedText);


    }

    private void applyAiResults(Analysis analysis, JsonNode jsonNode)
    {
        Double newPrice = jsonNode.get("estimatedNewPrice").asDouble();
        analysis.setEstimatedNewPrice(BigDecimal.valueOf(newPrice));

        Double newResPrice = jsonNode.get("estimatedResalePrice").asDouble();
        analysis.setEstimatedResalePrice(BigDecimal.valueOf(newResPrice));

        String newVerdict = jsonNode.get("verdict").asText();
        analysis.setVerdict(Verdict.valueOf(newVerdict));

        Double newSugPrice = jsonNode.get("suggestedPrice").asDouble();
        analysis.setSuggestedPrice(BigDecimal.valueOf(newSugPrice));






    }


    private void applyAiResultsWithPhoto(Analysis analysis, JsonNode jsonNode)
    {
        applyAiResults(analysis,jsonNode);


        String newAuthenticResult = jsonNode.get("authenticResult").asText();
        analysis.setAuthenticResult(AuthenticResult.valueOf(newAuthenticResult));



    }


}
