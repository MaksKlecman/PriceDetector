package com.example.PriceDetector.dto;

import com.example.PriceDetector.model.Category;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AnalysisRequest {

    @NotBlank(message = "can not be empty brand")

    private String itemBrand;

     @NotBlank(message = "can not be empty name")

    private String itemName;


    private Category category;

    @NotBlank(message = "please write down condition of your item for better verdict")

    private String condition;

    @NotBlank(message = "please write down description of your item for better verdict")

    private String description;

     @Min(value = 0, message = "price of item must be grater than 0")
    @Max(value = 99999, message = "price of item must be lower than 99999")
    private BigDecimal sellerPrice;
}
