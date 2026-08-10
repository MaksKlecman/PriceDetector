package com.example.PriceDetector.dto;

import com.example.PriceDetector.model.AuthenticResult;
import com.example.PriceDetector.model.Category;
import com.example.PriceDetector.model.Verdict;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AnalysisResponse {

    private String itemBrand;



    private String itemName;


    private Category category;



    private String condition;



    private String description;


    private BigDecimal sellerPrice;

    private BigDecimal estimatedNewPrice;

    private BigDecimal estimatedResalePrice;


    private Verdict verdict;


    private BigDecimal suggestedPrice;


    private String aiRawResponse;

    private AuthenticResult authenticResult;

}
