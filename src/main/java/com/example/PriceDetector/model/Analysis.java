package com.example.PriceDetector.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "analysis")

public class Analysis {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;


    private String itemBrand;

    private String itemName;

    @Enumerated(EnumType.STRING)
    private Category category;


    private String condition;


    private String description;


    private BigDecimal sellerPrice;


    private BigDecimal estimatedNewPrice;


    private BigDecimal estimatedResalePrice;

    @Enumerated(EnumType.STRING)
    private Verdict verdict;

    private BigDecimal suggestedPrice;

    @Column(columnDefinition = "TEXT")
    private String aiRawResponse;

    @Enumerated(EnumType.STRING)
    private AuthenticResult authenticResult;
















}
