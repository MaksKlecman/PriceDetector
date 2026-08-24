package com.example.PriceDetector.dto;

import com.example.PriceDetector.model.Analysis;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AnalysisMapper {

    public  AnalysisResponse toResponse(Analysis analysis)
    {
        AnalysisResponse response = new AnalysisResponse();
        response.setId(analysis.getId());
        response.setItemName(analysis.getItemName());
        response.setItemBrand(analysis.getItemBrand());
        response.setCategory(analysis.getCategory());
        response.setCondition(analysis.getCondition());
        response.setDescription(analysis.getDescription());
        response.setSellerPrice(analysis.getSellerPrice());
        response.setEstimatedNewPrice(analysis.getEstimatedNewPrice());
        response.setEstimatedResalePrice(analysis.getEstimatedResalePrice());
        response.setVerdict(analysis.getVerdict());
        response.setSuggestedPrice(analysis.getSuggestedPrice());
        response.setAiRawResponse(analysis.getAiRawResponse());
        response.setAuthenticResult(analysis.getAuthenticResult());
        response.setAdditionalNotes(analysis.getAdditionalNotes());
        return response;
    }

    public List<AnalysisResponse> toResponseList(List<Analysis> analyses)
    {
        List<AnalysisResponse> responseList = new ArrayList<>();
        for (int i = 0; i < analyses.size(); i++)
        {
            AnalysisResponse response = toResponse(analyses.get(i));

            responseList.add(response);
        }
        return responseList;
    }

    public Analysis toEntity(AnalysisRequest request)
    {
        Analysis analysis = new Analysis();
        analysis.setItemName(request.getItemName());
        analysis.setItemBrand(request.getItemBrand());
        analysis.setCategory(request.getCategory());
        analysis.setCondition(request.getCondition());
        analysis.setDescription(request.getDescription());
        analysis.setSellerPrice(request.getSellerPrice());
        analysis.setAdditionalNotes(request.getAdditionalNotes());
        return analysis;

    }

}
