package com.example.PriceDetector.service;

import com.example.PriceDetector.dto.AnalysisMapper;
import com.example.PriceDetector.dto.AnalysisRequest;
import com.example.PriceDetector.dto.AnalysisResponse;
import com.example.PriceDetector.exception.AnalysisNotFoundException;
import com.example.PriceDetector.model.Analysis;
import com.example.PriceDetector.repository.AnalysisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return "Analyze this item for resale value:\n" +
                "Brand: " + analysis.getItemBrand() + "\n" +
                "Item name: " + analysis.getItemName() + "\n" +
                "Category: " + analysis.getCategory() + "\n" +
                "Condition: " + analysis.getCondition() + "\n" +
                "Description: " + analysis.getDescription() + "\n" +
                "Seller price: " + analysis.getSellerPrice() +
                "Respond ONLY with valid JSON in this exact format:\n" +
                "{\n" +
                "  \"estimatedNewPrice\": <number>,\n" +
                "  \"estimatedResalePrice\": <number>,\n" +
                "  \"verdict\": \"BUY\" or \"DONT_BUY\" or \"NEGOTIATE\",\n" +
                "  \"suggestedPrice\": <number>\n" +
                "}";
    }

    @Transactional
    public AnalysisResponse runAiAnalysis(Long id)
    {
        Analysis analysis = getAnalysis(id);
        String prompt = buildPrompt(analysis);
        String aiResponse = aiClient.analyze(prompt);
        analysis.setAiRawResponse(aiResponse);
         Analysis saved = repository.save(analysis);
        return mapper.toResponse(saved);
    }


}
